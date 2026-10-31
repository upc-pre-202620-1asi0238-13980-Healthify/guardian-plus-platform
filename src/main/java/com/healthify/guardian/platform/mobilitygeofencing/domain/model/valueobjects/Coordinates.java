package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

public record Coordinates(Double latitude, Double longitude) {
    public Coordinates {
        if (!isValid(latitude, longitude)) {
            throw new IllegalArgumentException("Coordinates outside permissible range (-90/90 lat, -180/180 lon).");
        }
    }

    public static boolean isValid(Double latitude, Double longitude) {
        return latitude != null && longitude != null
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
    }
}
