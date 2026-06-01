package ru.practicum.ewm.model;

import java.time.LocalDateTime;

public interface EventShortView {
    Long getId();

    String getTitle();

    String getAnnotation();

    Long getCategoryId();

    String getCategoryName();

    LocalDateTime getEventDate();

    Long getInitiatorId();

    String getInitiatorName();

    Boolean getPaid();

    Integer getConfirmedRequests();
}
