package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventFullView;
import ru.practicum.ewm.model.EventShortView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByIdIn(Set<Long> ids);

    @Query("SELECT " +
            "e.id AS id, " +
            "e.title AS title, " +
            "e.annotation AS annotation, " +
            "e.description AS description, " +
            "e.category.id AS categoryId, " +
            "e.category.name AS categoryName, " +
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

    @Query(value = """
            SELECT
                e.id AS "id",
                e.title AS "title",
                e.annotation AS "annotation",
                e.description AS "description",
                e.category_id AS "categoryId",
                c.name AS "categoryName",
                e.initiator_id AS "initiatorId",
                u.name AS "initiatorName",
                l.lat AS "locationLat",
                l.lon AS "locationLon",
                e.paid AS "paid",
                e.request_moderation AS "requestModeration",
                e.participant_limit AS "participantLimit",
                e.created_on AS "createdOn",
                e.event_date AS "eventDate",
                e.published_on AS "publishedOn",
                e.state AS "state",
                COALESCE((SELECT COUNT(*)
                          FROM participation_requests pr
                          WHERE pr.event_id = e.id AND pr.status = 'CONFIRMED'), 0) AS "confirmedRequests"
            FROM events e
            JOIN categories c ON e.category_id = c.id
            JOIN users u ON e.initiator_id = u.id
            JOIN locations l ON e.location_id = l.id
            WHERE (:usersEmpty = TRUE OR e.initiator_id IN (:users))
              AND (:statesEmpty = TRUE OR e.state IN (:states))
              AND (:categoriesEmpty = TRUE OR e.category_id IN (:categories))
              AND e.event_date >= :rangeStart
              AND e.event_date <= :rangeEnd
            ORDER BY e.id ASC
            LIMIT :size OFFSET :from
            """,
            nativeQuery = true)
    List<EventFullView> findFullViewsByAdminFilters(@Param("users") List<Long> users,
                                                    @Param("usersEmpty") boolean usersEmpty,
                                                    @Param("states") List<String> states,
                                                    @Param("statesEmpty") boolean statesEmpty,
                                                    @Param("categories") List<Long> categories,
                                                    @Param("categoriesEmpty") boolean categoriesEmpty,
                                                    @Param("rangeStart") LocalDateTime rangeStart,
                                                    @Param("rangeEnd") LocalDateTime rangeEnd,
                                                    @Param("from") int from,
                                                    @Param("size") int size);

    @Query(value = """
            SELECT
                e.id AS "id",
                e.title AS "title",
                e.annotation AS "annotation",
                e.category_id AS "categoryId",
                c.name AS "categoryName",
                e.event_date AS "eventDate",
                e.initiator_id AS "initiatorId",
                u.name AS "initiatorName",
                e.paid AS "paid",
                COALESCE((SELECT COUNT(*)
                          FROM participation_requests pr
                          WHERE pr.event_id = e.id AND pr.status = 'CONFIRMED'), 0) AS "confirmedRequests"
            FROM events e
            JOIN categories c ON e.category_id = c.id
            JOIN users u ON e.initiator_id = u.id
            WHERE e.id IN (:ids)
            """,
            nativeQuery = true)
    List<EventShortView> findEventShortViewByIds(@Param("ids") Set<Long> ids);

    boolean existsByCategory_Id(Long categoryId);
}