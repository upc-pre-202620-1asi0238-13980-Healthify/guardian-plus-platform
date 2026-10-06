package com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands;

import java.util.UUID;

public record DeactivateSafeZoneCommand(UUID safeZoneId) {
    public DeactivateSafeZoneCommand {
        if (safeZoneId == null) throw new IllegalArgumentException("safeZoneId cannot be null.");
    }
}
