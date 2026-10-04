package com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands;

import java.util.UUID;

public record CreateSafeZoneCommand(
        UUID careRecipientProfileId,
        String name,
        Double latitude,
        Double longitude,
        Double radiusInMeters
) {}
