package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.stats.client.StatClient;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
@ComponentScan(value = {"ru.practicum.ewm", "ru.practicum.stats"})
public class MainApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(MainApplication.class, args);

        StatClient client = context.getBean(StatClient.class);
        client.hit("integrationTest", "/test", "192.168.1.1", LocalDateTime.now());
        List<ViewStatsDto> dtos = client.getStat(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), null, null);
        System.out.println(dtos);
    }
}
