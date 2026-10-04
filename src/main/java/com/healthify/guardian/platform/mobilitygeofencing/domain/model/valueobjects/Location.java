package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.time.Instant;

public record Location(Coordinates coordinates, Instant recordedAt, Double accuracyInMeters) {
    public Location {
        if (coordinates == null || recordedAt == null) {
            throw new IllegalArgumentException("Coordinates y recordedAt no pueden ser nulos.");
        }
        if (accuracyInMeters == null || accuracyInMeters < 0) {
            accuracyInMeters = 0.0;
        }
    }
}
