package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.LocationDto;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.EventFullView;
import ru.practicum.ewm.model.EventShortView;

import java.time.format.DateTimeFormatter;

public class EventMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EventFullDto toFullDto(EventFullView event, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .paid(event.getPaid())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .state(event.getState())
                .title(event.getTitle())
                .initiator(new UserShortDto(event.getInitiatorId(), event.getInitiatorName()))
                .location(new LocationDto(event.getLocation().getLat(), event.getLocation().getLon()))
                .eventDate(event.getEventDate().format(FORMATTER))
                .createdOn(event.getCreatedOn().format(FORMATTER))
                .publishedOn(event.getPublishedOn() == null ? null : event.getPublishedOn().format(FORMATTER))
                .description(event.getDescription())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .annotation(event.getAnnotation())
                .views(views == null ? 0L : views)
                .build();
    }

    public static EventShortDto toShortDto(EventShortView event, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .paid(event.getPaid())
                .category(
                        CategoryMapper.toDto(
                                new Category(event.getCategoryId(), event.getCategoryName())))
                .title(event.getTitle())
                .initiator(
                        new UserShortDto(event.getInitiatorId(), event.getInitiatorName()))
                .eventDate(event.getEventDate().format(FORMATTER))
                .annotation(event.getAnnotation())
                .views(views == null ? 0L : views)
                .build();
    }
}
