package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.*;

import java.util.List;

public interface EventService {

    EventFullDto addEvent(Long userId, NewEventDto dto);

    EventFullDto getOwnEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventShortDto> getOwnEvents(long userId, int from, int size);

    List<EventFullDto> getEventsByAdmin(AdminEventSearchParams params);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);
}
