package me.emmajiugo.spond.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.emmajiugo.spond.dto.Event;
import me.emmajiugo.spond.dto.WeatherForecast;
import me.emmajiugo.spond.service.WeatherService;
import me.emmajiugo.spond.validation.Validator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weather")
public class WeatherController {

    private final Validator validator;
    private final WeatherService weatherService;

    @PostMapping("/forecast")
    public ResponseEntity<WeatherForecast> getWeatherForecast(@Valid @RequestBody Event event) {
        log.info("""
                Received forecast request for event 
                #startTime: {}
                #endTime: {}
                """, event.startTime(), event.endTime());

        // validation
        validator.validateLocation(event.location());
        validator.validateEventTime(event.startTime(), event.endTime());

        WeatherForecast forecast = weatherService.getWeatherForecast(event);
        return ResponseEntity.ok(forecast);
    }
}
