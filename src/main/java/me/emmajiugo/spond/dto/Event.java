package me.emmajiugo.spond.dto;

import java.time.Instant;


public record Event(
        Instant startTime,
        Instant endTime,
        Location location
){}
