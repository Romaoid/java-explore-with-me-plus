package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    List<ParticipationRequestDto> getOwnParticipationRequests(long ownerId, long eventId);

    List<EventRequestStatusUpdateResult> updateOwnParticipationRequests(Long userId, Long eventId,
                                                                  EventRequestStatusUpdateRequest request);
}
