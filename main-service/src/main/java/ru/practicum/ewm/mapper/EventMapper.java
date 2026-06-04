package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.EventShortView;

import java.time.format.DateTimeFormatter;

public class EventMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EventShortDto toEventDto(EventShortView event, Long views) {
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
                .confirmedRequests(event.getConfirmedRequests())
                .views(views == null ? 0L : views)
                .build();
    }
}