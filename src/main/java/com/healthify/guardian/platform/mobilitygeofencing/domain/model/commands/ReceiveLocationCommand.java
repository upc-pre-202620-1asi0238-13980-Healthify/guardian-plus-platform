package com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Coordinates;

import java.time.Instant;
import java.util.UUID;

public record ReceiveLocationCommand(
        UUID fragileCitizenId,
        Coordinates coordinates,
        Double accuracyInMeters,
        Instant recordedAt
) {
    public ReceiveLocationCommand {
        if (fragileCitizenId == null) {
            throw new IllegalArgumentException("fragileCitizenId cannot be null.");
        }
        if (coordinates == null) {
            throw new IllegalArgumentException("coordinates cannot be null.");
        }
        if (recordedAt == null) {
            recordedAt = Instant.now();
        }
        if (accuracyInMeters == null || accuracyInMeters < 0) {
            accuracyInMeters = 0.0;
        }
    }
}
