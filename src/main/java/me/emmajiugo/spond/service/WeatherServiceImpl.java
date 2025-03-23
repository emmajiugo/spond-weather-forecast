package me.emmajiugo.spond.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.emmajiugo.spond.dto.Event;
import me.emmajiugo.spond.dto.Location;
import me.emmajiugo.spond.dto.WeatherForecast;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final RestClient restClient;
    private final Cache<String, JsonNode> weatherCacheManager;

    @Value("${weather.api.url}")
    private String weatherApiUrl;

    @Override
    public WeatherForecast getWeatherForecast(Event event) {
        log.info("""
                Fetching weather forecast for location: 
                #latitude: {}
                #longitude: {}
                """, event.location().getLatitude(), event.location().getLongitude());

        //JsonNode forecastData = fetchForecastData(event.location());
        //log.info(forecastData.toString());

        // Try to get from cache first
        String cacheKey = event.location().getCacheKey();
        JsonNode forecastData = weatherCacheManager.getIfPresent(cacheKey);

        // If not in cache, fetch from API and cache it
        if (forecastData == null) {
            log.info("Cache miss for key: {}", cacheKey);
            forecastData = fetchForecastData(event.location());
            weatherCacheManager.put(cacheKey, forecastData);
        } else {
            log.info("Cache hit for key: {}", cacheKey);
        }

        return findClosestForecast(forecastData, event);
    }

    private JsonNode fetchForecastData(Location location) {
        if (weatherApiUrl.isBlank()) {
            throw new RuntimeException("Weather API URL is not configured");
        }

        String url = String.format("%s?lat=%s&lon=%s",
                weatherApiUrl,
                location.getFormattedLatitude(),
                location.getFormattedLongitude());

        return restClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);
    }

    private WeatherForecast findClosestForecast(JsonNode forecastData, Event event) {
        // Extract meta information first
        JsonNode metaNode = forecastData.path("properties").path("meta");
        Instant lastUpdated = Instant.parse(metaNode.path("updated_at").asText());

        // Get the timeseries from the forecast data
        JsonNode timeseries = forecastData.path("properties").path("timeseries");

        if (timeseries.isMissingNode() || !timeseries.isArray() || timeseries.size() == 0) {
            throw new RuntimeException("Invalid forecast data received");
        }

        // Find the closest time entry to the event start time
        Instant eventStartTime = event.startTime();
        JsonNode closestTimestamp = null;
        long minDifference = Long.MAX_VALUE;

        for (JsonNode timeEntry : timeseries) {
            String timeString = timeEntry.path("time").asText();
            Instant forecastTime = Instant.parse(timeString);

            // Only consider forecasts before or at the event start time
            // and no more than 6 hours before (to ensure relevance)
            if (!forecastTime.isAfter(eventStartTime) &&
                    forecastTime.plus(6, ChronoUnit.HOURS).isAfter(eventStartTime)) {

                long difference = Math.abs(forecastTime.toEpochMilli() - eventStartTime.toEpochMilli());
                if (difference < minDifference) {
                    minDifference = difference;
                    closestTimestamp = timeEntry;
                }
            }
        }

        // If no suitable forecast found, try to find the closest future forecast
        if (closestTimestamp == null) {
            for (JsonNode timeEntry : timeseries) {
                String timeString = timeEntry.path("time").asText();
                Instant forecastTime = Instant.parse(timeString);

                // Only look at forecasts in the near future
                if (forecastTime.isAfter(eventStartTime) &&
                        forecastTime.isBefore(eventStartTime.plus(3, ChronoUnit.HOURS))) {

                    long difference = Math.abs(forecastTime.toEpochMilli() - eventStartTime.toEpochMilli());
                    if (difference < minDifference) {
                        minDifference = difference;
                        closestTimestamp = timeEntry;
                    }
                }
            }
        }

        if (closestTimestamp == null) {
            throw new RuntimeException("No suitable forecast found for the event time");
        }

        // Extract the weather data
        JsonNode details = closestTimestamp.path("data").path("instant").path("details");
        double temperature = details.path("air_temperature").asDouble();
        double windSpeed = details.path("wind_speed").asDouble();
        Instant forecastTime = Instant.parse(closestTimestamp.path("time").asText());

        return WeatherForecast.builder()
                .temperature(temperature)
                .windSpeed(windSpeed)
                .forecastTime(forecastTime)
                .lastUpdated(lastUpdated)
                .build();
    }
}
