package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.EventDto;
import ru.practicum.ewm.dto.UserDto;
import ru.practicum.ewm.model.Event;

import java.time.format.DateTimeFormatter;

public class EventMapper {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EventDto toEventDto(Event event) {
        return EventDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDto.builder()
                        .id(event.getCategory().getId())
                        .name(event.getCategory().getName())
                        .build())
                .confirmedRequests(0L) // Изменить после добавления запросов
                .eventDate(event.getEventDate().format(dateTimeFormatter))
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
