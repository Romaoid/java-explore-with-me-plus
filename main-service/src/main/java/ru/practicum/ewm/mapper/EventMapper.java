package ru.practicum.ewm.mapper;

import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.model.Event;

public class EventMapper {

    public static EventFullDto toFullDto(Event event) {
        return new EventFullDto();
//                EventFullDto.builder()
//                .title(event.getTitle())
//                .annotation(event.getAnnotation())
//                .description(event.getDescription())
//                .eventDate
                //код преобразования
    }
}
