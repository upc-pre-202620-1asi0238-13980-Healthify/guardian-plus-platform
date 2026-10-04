package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record SafeZoneResource(
        UUID id,
        UUID careRecipientProfileId,
        String name,
        Double latitude,
        Double longitude,
        Double radiusInMeters,
        Boolean active
) {}