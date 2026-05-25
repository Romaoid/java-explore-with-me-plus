package ru.practicum.stats.server.model;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Data
public class ReqParam {
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean unique;
    private List<String> uris;

    public ReqParam(@RequestParam("start") String start,
                    @RequestParam("end") String end,
                    @RequestParam(value = "unique", defaultValue = "false") boolean unique,
                    @RequestParam(value = "uris", required = false) List<String> uris) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.start = LocalDateTime.parse(start, formatter);
        this.end = LocalDateTime.parse(end, formatter);
        this.unique = unique;
        this.uris = uris != null ? uris : Collections.emptyList();
    }
}
