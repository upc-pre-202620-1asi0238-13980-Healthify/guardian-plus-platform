package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record LocationTrackingResource(
        UUID fragileCitizenId,
        Double latitude,
        Double longitude,
        String status,
        Instant lastUpdatedAt
) {}
