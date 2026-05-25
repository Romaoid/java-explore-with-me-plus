package ru.practicum.stats.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.ReqParam;
import ru.practicum.stats.server.service.StatService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatController {
    private final StatService service;

    @PostMapping(path = "/hit")
    public void addStat(@RequestBody EndpointHitDto dto) {
        //satatuscode201
        //add logging
        //add error-handler
        service.addStat(dto);
    }

    @GetMapping(path = "/stats")
    public List<ViewStatsDto> getStats(@Valid ReqParam params) {
            //@RequestParam String start,
//                         @RequestParam String end,
//                         @RequestParam(defaultValue = "false", required = false) boolean unique,
//                         @RequestParam(defaultValue = "", required = false) List<String> uris) {

        return service.getStats(params);
    }
}
