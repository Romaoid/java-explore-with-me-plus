package ru.practicum.ewm.model;

import java.time.LocalDateTime;

public interface EventFullView {
    Long getId();

    String getTitle();

    String getAnnotation();

    String getDescription();

    Category getCategory();

    Long getInitiatorId();

    String getInitiatorName();

    Location getLocation();

    Boolean getPaid();

    Boolean getRequestModeration();

    Integer getParticipantLimit();

    Integer getConfirmedRequests();

    LocalDateTime getCreatedOn();

    LocalDateTime getEventDate();

    LocalDateTime getPublishedOn();

    EventState getState();
}
