package com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands;

import java.util.UUID;

public record ActivateSafeZoneCommand(UUID safeZoneId) {
    public ActivateSafeZoneCommand {
        if (safeZoneId == null) throw new IllegalArgumentException("safeZoneId is not null");
    }
}
