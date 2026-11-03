package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.util.UUID;

public record CreateSafeZoneResource(
        UUID fragileCitizenId,
        String name,
        Double centerLatitude,
        Double centerLongitude,
        Double radiusInMeters
) {}
