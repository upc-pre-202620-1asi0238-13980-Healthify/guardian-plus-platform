package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record CreateSafeZoneResource(
        UUID careRecipientProfileId,
        String name,
        Double latitude,
        Double longitude,
        Double radiusInMeters
) {}
