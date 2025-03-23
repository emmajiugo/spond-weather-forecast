package me.emmajiugo.spond.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    @Digits(integer = 3, fraction = 4)
    private double latitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    @Digits(integer = 3, fraction = 4)
    private double longitude;

    public String getFormattedLatitude() {
        return String.format("%.4f", latitude);
    }

    public String getFormattedLongitude() {
        return String.format("%.4f", longitude);
    }

    public String getCacheKey() {
        return getFormattedLatitude() + ";" + getFormattedLongitude();
    }
}
