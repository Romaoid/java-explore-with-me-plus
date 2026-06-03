package ru.practicum.ewm.model;

import java.time.LocalDateTime;

public interface EventFullView {
    Long getId();

    String getTitle();

    String getAnnotation();

    String getDescription();

    Long getCategoryId();

    String getCategoryName();

    Long getInitiatorId();

    String getInitiatorName();

    Float getLocationLat();

    Float getLocationLon();

    Boolean getPaid();

    Boolean getRequestModeration();

    Integer getParticipantLimit();

    Integer getConfirmedRequests();

    LocalDateTime getCreatedOn();

    LocalDateTime getEventDate();

    LocalDateTime getPublishedOn();

    EventState getState();
}
