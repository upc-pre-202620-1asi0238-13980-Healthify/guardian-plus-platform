package com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands;

import java.util.UUID;

public record DeleteSafeZoneCommand(UUID safeZoneId) {
    public DeleteSafeZoneCommand {
        if (safeZoneId == null) throw new IllegalArgumentException("safeZoneId no puede ser nulo.");
    }
}