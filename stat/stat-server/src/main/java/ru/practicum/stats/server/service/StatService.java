package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.dao.StatStorage;
import ru.practicum.stats.server.model.App;
import ru.practicum.stats.server.model.ReqParam;
import ru.practicum.stats.server.model.Stat;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatService {
    private final StatStorage storage;

    @Transactional
    public void addStat(EndpointHitDto dto) {
        //notNull?


        Stat newStatRecoding = Stat.builder()
                .app(new App(dto.getApp()))
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();

        //add Logging
        storage.save(newStatRecoding);
    }

    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(ReqParam params) {}
}
