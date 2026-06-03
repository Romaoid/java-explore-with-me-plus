package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dao.EventRepository;
import ru.practicum.ewm.dao.ParticipationRequestRepository;
import ru.practicum.ewm.dao.UserRepository;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private static final Logger log = LoggerFactory.getLogger(ParticipationRequestServiceImpl.class);
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    public List<ParticipationRequestDto> getOwnParticipationRequests(long ownerId, long eventId) {
        getEventIfExistWithOwnerValidation(eventId, ownerId);

        List<ParticipationRequest> requests =
                requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

        return requests.stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateOwnParticipationRequests(Long ownerId, Long eventId,
                                                                         EventRequestStatusUpdateRequest request) {
        //проверяем, событие существует, юзер - создатель
        Event event = getEventIfExistWithOwnerValidation(eventId, ownerId);

        //проверяем для события лимит заявок не равен 0 или включена пре-модерация заявок
        long limit = event.getParticipantLimit();
        if (limit == 0) {
            throw new ConflictException("The participant limit has been reached");
        }
        if (!event.getRequestModeration()) {
            throw new ConflictException("The request moderation is disabled");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEventId(eventId);

        //проверяем, что лимит для заявок не исчерпан
        long countConfirmed = requests.stream()
                .filter(req -> req.getStatus() == RequestStatus.CONFIRMED)
                .count();

        if (countConfirmed >= limit) {
            throw new ConflictException("The participant limit has been reached");
        }

        //проверяем, что заявки в состоянии Ожидания
        Set<Long> idsToCheck = new HashSet<>(request.getRequestIds());

        boolean allPending = requests.stream()
                .filter(req -> idsToCheck.contains(req.getId()))
                .allMatch(req -> req.getStatus() == RequestStatus.PENDING);

        if (!allPending) {
            throw new ValidationException("Request must have status PENDING");
        }

        //Подтверждаем до лимита и отклоняем остальное или отклоняем все
        List<ParticipationRequest> updatedRequests = new ArrayList<>();
        List<ParticipationRequest> toConfirm;
        List<ParticipationRequest> toReject;
        if (request.getStatus() != RequestStatus.PENDING) {
            long availableSlots = limit - countConfirmed;

            List<ParticipationRequest> targetRequests = requests.stream()
                    .filter(req -> request.getRequestIds().contains(req.getId()))
                    .toList();

            toConfirm = targetRequests.stream().limit(availableSlots).toList();
            toReject = targetRequests.stream().skip(availableSlots).toList();

            toConfirm.forEach(req -> req.setStatus(RequestStatus.CONFIRMED));
            toReject.forEach(req -> req.setStatus(RequestStatus.REJECTED));

            updatedRequests.addAll(toConfirm);
            updatedRequests.addAll(toReject);

            log.info("Save to RequestRepository updated entities: {}", updatedRequests);
            requestRepository.saveAll(updatedRequests);
        } else {
            throw new ValidationException("Field: status. Error: must be CONFIRMED or REJECTED. Value: " +
                    request.getStatus());
        }

        //формирование ответа
        List<ParticipationRequestDto> confirmed = toConfirm.stream().map(RequestMapper::toDto).toList();
        List<ParticipationRequestDto> rejected = toReject.stream().map(RequestMapper::toDto).toList();

        return new EventRequestStatusUpdateResult(confirmed, rejected);
    }

    @Override
    public List<ParticipationRequestDto> getOwnRequests(Long userId) {
        return requestRepository.findAllByRequesterId(userId).stream()
                .map(RequestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addOwnRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Field: eventId. Error: event не найден. Value: " + eventId));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Field: eventId. Error: event не найден. Value: " + eventId);
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("The request to own event is rejected");
        }

        int limit = event.getParticipantLimit();
        int confirmedRequests = eventRepository.findFullViewById(eventId)
                .orElseThrow(() -> new ConflictException("Event not found"))
                .getConfirmedRequests();
        if (limit == 0 || limit == confirmedRequests) {
            throw new ConflictException("The participant limit has been reached to event id: " + eventId);
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("The request to event id: " + eventId +
                    " from user id: " + userId + "is already exists.");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setEvent(event);
        request.setRequester(userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User id: " + userId + " not found.")));
        if (event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        log.info("Save to RequestRepository entity: {}", request);

        request = requestRepository.save(request);

        log.info("request saved with id: {}", request.getId());

        return RequestMapper.toDto(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelOwnRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id= " + requestId + " was not found"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new NotFoundException("Request with id= " + requestId + " was not found");
        }

        log.info("Delete from RequestRepository request: {}", request);
        requestRepository.delete(request);

        return RequestMapper.toDto(request);
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
