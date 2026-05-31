package ru.practicum.ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.NewEventDto;

public interface EventService {

    EventFullDto addEvent(Long userId, NewEventDto dto, HttpServletRequest requestInfo);
}
