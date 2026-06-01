package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dao.ParticipationRequestRepository;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;

    @Override
    public List<ParticipationRequestDto> getOwnParticipationRequests(long ownerId, long eventId) {
        getEventIfExistWithOwnerValidation(eventId, ownerId);

        List<ParticipationRequest> requests =
                requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

        return requests.isEmpty() ? new ArrayList<>() : requests.stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    public List<EventRequestStatusUpdateResult> updateOwnParticipationRequests(Long ownerId, Long eventId,
                                                                         EventRequestStatusUpdateRequest request) {
        Event event = getEventIfExistWithOwnerValidation(eventId, ownerId);

        if (event.getParticipantLimit() == 0) {
            throw new ConflictException("The participant limit has been reached");
        }

        if (event.getRequestModeration() == false) {
            throw new ConflictException("The request moderation is disabled");
        }

        //если отклонение?
        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);

        long countConfirmed = requests.stream()
                .filter(req -> req.getStatus() == RequestStatus.CONFIRMED)
                .count();

        if (countConfirmed >= event.getParticipantLimit()) {
            throw new ConflictException("The request limit for this event has been reached.");
        }

        /*
        статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
         */

        return Collections.emptyList();//Доработать
    }

    private Event getEventIfExistWithOwnerValidation(long eventId, long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Field: eventId. Error: event не найден. Value: " + eventId));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Field: userId. Error: Initiator has another id. Value: " + userId);
        }
        return event;
    }
}
