package ru.practicum.ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.CategoryRepository;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dao.LocationRepository;
import ru.practicum.ewm.dao.UserRepository;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.dto.UpdateEventUserRequest;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.stats.client.StatClient;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final StatClient statClient;

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto dto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new ConflictException("Field: initiator. Error: id не найден. Value: " + userId));
        Category category = getCategoryByIdWithValidation(dto.getCategory());
        Location location = getLocation(dto.getLocation().getLat(), dto.getLocation().getLon());
        LocalDateTime eventDate = getEventDateWithValidation(dto.getEventDate());

        Event newEvent = Event.builder()
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .eventDate(eventDate)
                .category(category)
                .initiator(initiator)
                .location(location)
                .participantLimit(dto.getParticipantLimit())
                .paid(dto.getPaid())
                .requestModeration(dto.getRequestModeration())
                .build();
        log.info("Запись в базу данных объекта Event: {}", newEvent);

        newEvent = eventRepository.save(newEvent);
        log.info("id объекта: {}", newEvent.getId());

        return eventRepository.findFullViewById(newEvent.getId())
                .map(view -> EventMapper.toFullDto(view, 0L))
                .orElseThrow(() -> new RuntimeException("Ошибка при выгрузке представления FullView в методе addEvent"));
    }

    @Override
    public EventFullDto getOwnEvent(Long userId, Long eventId) {
        Event event = getEventIfExistWithOwnerValidation(eventId, userId);

        ViewStatsDto stat = getStatByEvent(event);

        return eventRepository.findFullViewById(event.getId())
                .map(view -> EventMapper.toFullDto(view, stat.getHits()))
                .orElseThrow(() -> new RuntimeException("Ошибка при выгрузке представления FullView в методе addEvent"));
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId,
                                    UpdateEventUserRequest request) {
        Event event = getEventIfExistWithOwnerValidation(eventId, userId);

        if (event.getState() == EventState.PUBLISHED) {
            throw new ValidationException("Event must not be published");
        }

        if (request.getEventDate() != null) {
            LocalDateTime eventDate = getEventDateWithValidation(request.getEventDate());
            event.setEventDate(eventDate);
        }

        if (request.getCategory() != null) {
            Category category = getCategoryByIdWithValidation(request.getCategory());
            event.setCategory(category);
        }

        if (request.getLocation() != null) {
            if (request.getLocation().getLat() != null && request.getLocation().getLon() != null) {
                Location location = getLocation(request.getLocation().getLat(), request.getLocation().getLon());
                event.setLocation(location);
            }
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getStateAction() != null) {
            EventState state = validateStateAction(request.getStateAction(), event.getState());
            event.setState(state);
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        log.info("Запись в базу данных обновленного объекта Event: {}", event);
        eventRepository.save(event);

        ViewStatsDto stat = getStatByEvent(event);

        return eventRepository.findFullViewById(event.getId())
                .map(view -> EventMapper.toFullDto(view, stat.getHits()))
                .orElseThrow(() -> new RuntimeException("Ошибка при выгрузке представления FullView в методе addEvent"));
    }

    @Override
    public List<EventShortDto> getOwnEvents(long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new ValidationException("Field: userId. Error: id не найден. Value: " + userId);
        }

        List<EventShortView> events = eventRepository.findShortViewsById(userId, from, size);
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        Map<Long, Long> stats = getStatsByUris(LocalDateTime.MIN, LocalDateTime.now(), uris, false);

        return events.stream()
                .map(view -> EventMapper.toShortDto(view, stats.get(view.getId())))
                .sorted(Comparator.comparingLong(EventShortDto::getViews).reversed()) //Важен порядок выдачи? зачем мы сортировали Дто статистики по просмотрам?
                .toList();
    }

//private******************************************************
    private Location getLocation(Float Lat, Float Lon) {
        return locationRepository.findByLatAndLon(Lat, Lon)
                .orElseGet(() -> locationRepository.save(new Location(Lat, Lon)));
    }

    private Event getEventIfExistWithOwnerValidation(long eventId, long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Field: eventId. Error: event не найден. Value: " + eventId));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Field: userId. Error: Initiator has another id. Value: " + userId);
        }
        return event;
    }

    private Category getCategoryByIdWithValidation(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Категория с id=" + id + " не найдена"));
    }

    private LocalDateTime getEventDateWithValidation(String date) {
        LocalDateTime eventDate;
        if (!date.isBlank()) {
            eventDate = LocalDateTime.parse(date, FORMATTER);

            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ConflictException("Field: eventDate. Error: Начало события должно быть позже" +
                        LocalDateTime.now().plusHours(2) + ". Value: " + eventDate);
            }
        } else {
            eventDate = null;
        }
        return eventDate;
    }

    private ViewStatsDto getStatByEvent(Event event) {
        String uri = "/event/" + event.getId();

        log.info("Запрос статистики из stat-db для события: {}", event.getId());
        return statClient.getStat(
                        event.getCreated(),
                        LocalDateTime.now(),
                        List.of(uri),
                        false)
                .getFirst();
    }

    private EventState validateStateAction(String stateAction, EventState eventState) {
        switch (stateAction.trim().toUpperCase()) {
            case "SEND_TO_REVIEW" -> {
                if (eventState != EventState.CANCELED) {
                    throw new ConflictException("Only pending or canceled events can be changed");
                }
                return EventState.PENDING;
            }
            case "CANCEL_REVIEW" -> {
                if (eventState != EventState.PENDING) {
                    throw new ConflictException("Only pending or canceled events can be changed");
                }
                return EventState.CANCELED;
            }
            default -> throw new ValidationException("Field: stateAction. Error: must be SEND_TO_REVIEW or " +
                    "CANCEL_REVIEW. Value: " + stateAction);
        }
    }

    private Map<Long, Long> getStatsByUris(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique) {
        //List<String> uris = events.stream().map(event -> "/event/" + event.getId()).toList();

        List<ViewStatsDto> stats = statClient.getStat(start, end, uris, unique);

        if (stats == null) {
            return Collections.emptyMap();
        }

        Map<Long, Long> viewsMap = new LinkedHashMap<>();
        for (ViewStatsDto dto : stats) {
            String uri = dto.getUri();
            Long id = Long.parseLong(
                    uri.substring(
                    uri.lastIndexOf('/') + 1));
            viewsMap.put(id, dto.getHits());
        }

        return viewsMap;
    }
}
