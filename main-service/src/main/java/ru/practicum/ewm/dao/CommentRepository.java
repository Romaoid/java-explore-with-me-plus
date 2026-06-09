package ru.practicum.ewm.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    @Query("SELECT c FROM Comment c " +
            "WHERE (:text IS NULL OR LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:eventId IS NULL OR c.event.id = :eventId) " +
            "AND (:authorId IS NULL OR c.author.id = :authorId)")
    List<Comment> findCommentsAdmin(@Param("text") String text, @Param("eventId") Long eventId,
                                    @Param("authorId") Long authorId, Pageable pageable);
}