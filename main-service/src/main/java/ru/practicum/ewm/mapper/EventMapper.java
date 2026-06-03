package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.model.Event;

import java.time.format.DateTimeFormatter;

public class EventMapper {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EventShortDto toEventDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDto.builder()
                        .id(event.getCategory().getId())
                        .name(event.getCategory().getName())
                        .build())
                .confirmedRequests(0L) // Изменить после добавления запросов
                .eventDate(event.getEventDate().format(DATE_TIME_FORMATTER))
                .initiator(UserDto.builder()
                        .id(event.getInitiator().getId())
                        .name(event.getInitiator().getName())
                        .email(event.getInitiator().getEmail())
                        .build())
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(0L) // Доработать во время выполнения статистики
                .build();
    }
}
