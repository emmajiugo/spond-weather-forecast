package me.emmajiugo.spond.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import me.emmajiugo.spond.dto.Event;
import me.emmajiugo.spond.dto.Location;
import me.emmajiugo.spond.dto.WeatherForecast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private Cache<String, JsonNode> weatherCacheManager;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode sampleResponse;

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(weatherService, "weatherApiUrl", "https://api.met.no/weatherapi/locationforecast/2.0/complete");
    }

    @Test
    void testGetWeatherForecast() throws JsonProcessingException {

        Instant startTime = Instant.parse("2025-03-23T09:30:00Z");
        Location location = new Location(59.9230, 10.5939);
        var testEvent = new Event(startTime, startTime.plus(2, ChronoUnit.HOURS), location);

        // sample response
        String sampleJson = "{ \"type\": \"Feature\", \"geometry\": { \"type\": \"Point\", \"coordinates\": [ 10.5939, 59.923, 81 ] }, \"properties\": { \"meta\": { \"updated_at\": \"2025-03-23T06:27:21Z\" }, \"timeseries\": [ { \"time\": \"2025-03-23T09:00:00Z\", \"data\": { \"instant\": { \"details\": { \"air_temperature\": 4.2, \"wind_speed\": 3.4 } } } }, { \"time\": \"2025-03-23T10:00:00Z\", \"data\": { \"instant\": { \"details\": { \"air_temperature\": 5.7, \"wind_speed\": 3.8 } } } } ] } }";

        sampleResponse = objectMapper.readTree(sampleJson);

        when(weatherCacheManager.get(anyString(), any())).thenReturn(sampleResponse);

        WeatherForecast forecast = weatherService.getWeatherForecast(testEvent);

        assertNotNull(forecast);
        assertEquals(4.2, forecast.getTemperature());
        assertEquals(3.4, forecast.getWindSpeed());
        assertEquals(Instant.parse("2025-03-23T09:00:00Z"), forecast.getForecastTime());
        assertEquals(Instant.parse("2025-03-23T06:27:21Z"), forecast.getLastUpdated());
    }

    @Test
    void testGetForecastForEventInProgress() throws IOException {

        Instant now = Instant.now();
        Instant eventStart = now.minus(1, ChronoUnit.HOURS);
        Instant eventEnd = now.plus(1, ChronoUnit.HOURS);
        Location location = new Location(59.9230, 10.5939);
        var currentEvent = new Event(eventStart, eventEnd, location);

        // Create a response with forecast for current time
        String currentTimeJson = String.format("{ \"type\": \"Feature\", \"properties\": { \"meta\": { \"updated_at\": \"2025-03-23T06:27:21Z\" }, \"timeseries\": [ { \"time\": \"%s\", \"data\": { \"instant\": { \"details\": { \"air_temperature\": 7.5, \"wind_speed\": 4.2 } } } } ] } }",
                now.minus(30, ChronoUnit.MINUTES).toString());

        JsonNode currentResponse = objectMapper.readTree(currentTimeJson);

        when(weatherCacheManager.get(anyString(), any())).thenReturn(currentResponse);

        WeatherForecast forecast = weatherService.getWeatherForecast(currentEvent);

        assertNotNull(forecast);
        assertEquals(7.5, forecast.getTemperature());
        assertEquals(4.2, forecast.getWindSpeed());
    }
}