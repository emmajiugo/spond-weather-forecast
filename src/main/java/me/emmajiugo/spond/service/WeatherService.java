package me.emmajiugo.spond.service;

import me.emmajiugo.spond.dto.Event;
import me.emmajiugo.spond.dto.WeatherForecast;

public interface WeatherService {
    WeatherForecast getWeatherForecast(Event event);
}
