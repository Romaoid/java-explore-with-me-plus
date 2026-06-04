package ru.practicum.ewm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateEventAdminRequest {
    private String title;
    private String annotation;
    private String description;
    private String eventDate;
    private Long category;
    private Integer participantLimit;
    private LocationDto location;
    private Boolean paid;
    private String stateAction;
    private Boolean requestModeration;
}