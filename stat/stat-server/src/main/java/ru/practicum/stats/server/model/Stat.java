package ru.practicum.stats.server.model;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "statistics")
@Getter
@Setter
@ToString
@Builder
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "app_id")
    private App app;

    private String uri;
    private String ip;
    private LocalDateTime timestamp;

    public String getAppName() {
        return app != null ? app.getName() : null;
    }
}
