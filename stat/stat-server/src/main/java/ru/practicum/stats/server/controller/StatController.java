package ru.practicum.stats.server.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatController {
    private final StatService service;

    @PostMapping(path = "/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addStat(@RequestBody EndpointHitDto dto) {
        log.info("Вызван эндпоинт добавления запроса с данными: {}", dto);
        service.addStat(dto);
    }

    @GetMapping(path = "/stats")
    public List<ViewStatsDto> getStats(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                       @RequestParam(defaultValue = "false") boolean unique,
                                       @RequestParam(required = false) List<String> uris) {
        log.info("Вызван эндпоинт на получение списка статистики с параметрами: start = {},\nend = {},\nunique = {},\nuris = {}",
                start, end, unique, uris);

        uris = (uris == null || uris.isEmpty()) ? uris : null;

        return service.getStats(start, end, unique, uris);
    }
}
