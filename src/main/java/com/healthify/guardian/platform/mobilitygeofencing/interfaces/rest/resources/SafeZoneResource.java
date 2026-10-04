package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record SafeZoneResource(
        UUID id,
        UUID fragileCitizenId,
        String name,
        Double latitude,
        Double longitude,
        Double radiusInMeters,
        String status
) {}