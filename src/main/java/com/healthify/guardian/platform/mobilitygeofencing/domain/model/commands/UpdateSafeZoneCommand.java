package com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Coordinates;

import java.util.UUID;

public record UpdateSafeZoneCommand(
        UUID safeZoneId,
        String name,
        Coordinates center,
        Double radiusInMeters
) {
    public UpdateSafeZoneCommand {
        if (safeZoneId == null) throw new IllegalArgumentException("safeZoneId cannot be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("The geofence name is required.");
        if (center == null) throw new IllegalArgumentException("The coordinates of the center are required.");
        if (radiusInMeters == null || radiusInMeters <= 0) throw new IllegalArgumentException("The radius must be greater than 0 meters.");
    }
}
