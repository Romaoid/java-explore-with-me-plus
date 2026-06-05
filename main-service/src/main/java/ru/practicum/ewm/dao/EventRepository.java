package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventFullView;
import ru.practicum.ewm.model.EventShortView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByIdIn(Set<Long> ids);

    @Query("SELECT " +
            "e.id AS id, " +
            "e.title AS title, " +
            "e.annotation AS annotation, " +
            "e.description AS description, " +
            "e.category AS categoryId, " +
            "e.category AS categoryName, " +
            "e.initiator.id AS initiatorId, " +
            "e.initiator.name AS initiatorName, " +
            "e.location.lat AS locationLat, " +
            "e.location.lon AS locationLon, " +
            "e.paid AS paid, " +
            "e.requestModeration AS requestModeration, " +
            "e.participantLimit AS participantLimit, " +
            "e.created AS createdOn, " +
            "e.eventDate AS eventDate, " +
            "e.published AS publishedOn, " +
            "e.state AS state, " +
            "(SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event = e AND r.status = 'CONFIRMED') AS confirmedRequests " +
            "FROM Event e " +
            "WHERE e.id = :eventId")
    Optional<EventFullView> findFullViewById(@Param("eventId") Long id);

    @Query(value = "SELECT " +
            "e.id, " +
            "e.title, " +
            "e.annotation, " +
            "e.category_id, " +
            "c.name, " +
            "e.event_date, " +
            "e.initiator_id, " +
            "u.name, " +
            "e.paid, " +
            "COALESCE((SELECT COUNT(*) " +
            "FROM participation_requests pr WHERE pr.event_id = e.id AND pr.status = 'CONFIRMED'), 0) " +
            "FROM events e " +
            "JOIN categories c ON e.category_id = c.id " +
            "JOIN users u ON e.initiator_id = u.id " +
            "WHERE e.initiator_id = :userId " +
            "ORDER BY e.event_date DESC " +
            "OFFSET :from LIMIT :size",
    nativeQuery = true)
    List<EventShortView> findShortViewsById(@Param("userId") long id,
                                            @Param("from") long from,
                                            @Param("size") long size);

    @Query("SELECT " +
            "e.id AS id, " +
            "e.title AS title, " +
            "e.annotation AS annotation, " +
            "e.category.id AS categoryId, " +
            "e.category.name AS categoryName, " +
            "e.eventDate AS eventDate, " +
            "e.initiator.id AS initiatorId, " +
            "e.initiator.name AS initiatorName, " +
            "e.paid AS paid, " +
            "(SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event = e AND r.status = 'CONFIRMED') AS confirmedRequests " +
            "FROM Event e " +
            "WHERE e.id IN :ids")
    List<EventShortView> findEventShortViewByIds(@Param("ids") Set<Long> ids);

    @Query(value = "SELECT " +
            "e.id, " +
            "e.title, " +
            "e.annotation, " +
            "e.category_id, " +
            "c.name, " +
            "e.event_date, " +
            "e.initiator_id, " +
            "u.name, " +
            "e.paid, " +
            "COALESCE((SELECT COUNT(*) " +
            "FROM participation_requests pr WHERE pr.event_id = e.id AND pr.status = 'CONFIRMED'), 0) " +
            "FROM events e " +
            "JOIN categories c ON e.category_id = c.id " +
            "JOIN users u ON e.initiator_id = u.id " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:categories IS NULL OR e.category_id IN (:categories)) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.event_date >= :rangeStart " +
            "AND (:rangeEnd IS NULL OR e.event_date <= :rangeEnd) " +
            "AND (:onlyAvailable = false OR e.participant_limit = 0 OR e.participant_limit > " +
            "COALESCE((SELECT COUNT(*) FROM participation_requests pr2 " +
            "WHERE pr2.event_id = e.id AND pr2.status = 'CONFIRMED'), 0)) " +
            "ORDER BY e.event_date ASC " +
            "OFFSET :from LIMIT :size",
            nativeQuery = true)
    List<EventShortView> findPublicEvents(@Param("text") String text,
                                          @Param("categories") List<Long> categories,
                                          @Param("paid") Boolean paid,
                                          @Param("rangeStart") LocalDateTime rangeStart,
                                          @Param("rangeEnd") LocalDateTime rangeEnd,
                                          @Param("onlyAvailable") Boolean onlyAvailable,
                                          @Param("from") int from,
                                          @Param("size") int size);
}