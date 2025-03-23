package me.emmajiugo.spond.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeatherForecast {
    private double temperature;
    private String temperatureUnit;
    private double windSpeed;
    private String windSpeedUnit;
    private Instant forecastTime;
    private Instant lastUpdated;

    public String getTemperatureUnit() {
        return "celsius";
    }

    public String getWindSpeedUnit() {
        return "m/s";
    }
}
