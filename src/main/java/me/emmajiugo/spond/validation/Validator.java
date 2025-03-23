package me.emmajiugo.spond.validation;

import me.emmajiugo.spond.dto.Location;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class Validator {

    public void validateLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Event must have a location");
        }
    }

    public void validateEventTime(Instant startTime, Instant endTime) {
        Instant now = Instant.now();
        Instant sevenDaysFromNow = now.plus(7, ChronoUnit.DAYS);

        if (startTime.isAfter(sevenDaysFromNow)) {
            throw new IllegalArgumentException("Event must start within the next 7 days");
        }

        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Event end time must be after start time");
        }
    }
}
