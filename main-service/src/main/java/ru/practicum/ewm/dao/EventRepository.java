package ru.practicum.ewm.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventFullView;
import ru.practicum.ewm.model.EventShortView;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT " +
            "e.id AS id, " +
            "e.title AS title, " +
            "e.annotation AS annotation, " +
            "e.description AS description, " +
            "e.category AS category, " +
            "e.initiator.id AS initiatorId, " +
            "e.initiator.name AS initiatorName, " +
            "e.location AS location, " +
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

    @Query("SELECT " +
            "e.id AS id, " +
            "e.title AS title, " +
            "e.annotation AS annotation, " +
            "e.category AS category, " +
            "e.initiator.id AS initiatorId, " +
            "e.initiator.name AS initiatorName, " +
            "e.paid AS paid, " +
            "e.eventDate AS eventDate, " +
            "(SELECT COUNT(r) FROM ParticipationRequest r WHERE r.event = e AND r.status = 'CONFIRMED') AS confirmedRequests " +
            "FROM Event e " +
            "WHERE e.id = :eventId")
    List<EventShortView> findShortViewsById(@Param("eventId") long id,
                                            @Param("from") long from,
                                            @Param("size") long size);
}
