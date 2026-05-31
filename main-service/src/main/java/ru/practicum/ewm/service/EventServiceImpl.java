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
import ru.practicum.ewm.dto.NewEventDto;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;
import ru.practicum.stats.client.StatClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

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
    public EventFullDto addEvent(Long userId, NewEventDto dto, HttpServletRequest requestInfo) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + dto.getCategory() + " не найдена"));
        Location location = locationRepository.findByLatAndLon(dto.getLocation().getLat(), dto.getLocation().getLon())
                .orElseGet(() -> locationRepository.save(
                        new Location(dto.getLocation().getLat(), dto.getLocation().getLon()))
                );

        Event newEvent = Event.builder()
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .eventDate(LocalDateTime.parse(dto.getEventDate(), FORMATTER))
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

        statClient.hit("main-service", "/events/" + newEvent.getId(),
                requestInfo.getRemoteAddr(), newEvent.getCreated());

        EventFullDto fullDto = getFullDto(List.of(newEvent)).getFirst();
        //Или ручками прописать fullDto.setViews(0)

        return fullDto;
    }

    private List<EventFullDto> getFullDto(List<Event> events) {
        List<String> uris = events.stream().map(event -> "/event/" + event.getId()).toList();

        return Collections.emptyList();
    }
}
