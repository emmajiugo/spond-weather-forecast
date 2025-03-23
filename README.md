# Spond-Weather-Forecast

### Demo app

This service provides weather forecasts for Spond registered events.

## Features

- Fetches weather data from met.no API
- Returns air temperature and wind speed for events
- Caches responses to minimize API calls and improve performance

## Requirements

- Java 17 or higher

## Running the Application

1. Clone the repository
2. Build the application:
   ```
   mvn clean package
   ```
3. Run the application as a default Spring Boot application or with the following command:
   ```
   java -jar target/spond-0.0.1-SNAPSHOT.jar
   ```

The service will be available at `http://localhost:8080`.

## API Usage

### Get Weather Forecast for an Event

```
POST /api/weather/forecast
Content-Type: application/json

{
    "startTime": "2025-03-24T14:00:00Z",
    "endTime": "2025-03-24T16:00:00Z",
    "location": {
        "latitude": 59.9230,
        "longitude": 10.5939
    }
}
```

Response:

```
{
    "temperature": 9.0,
    "temperatureUnit": "celsius",
    "windSpeed": 1.5,
    "windSpeedUnit": "m/s",
    "forecastTime": "2025-03-24T14:00:00Z",
    "lastUpdated": "2025-03-23T16:27:09Z"
}
```

## Future Improvements

- Add more error handling and retry logic for API call
- Add metrics for monitoring
- Set up distributed caching for multi-instance deployments
- Implement rate limiting for better resource management (*)
