package ru.practicum.ewm.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventShortView;

import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByIdIn(Set<Long> ids);

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
}