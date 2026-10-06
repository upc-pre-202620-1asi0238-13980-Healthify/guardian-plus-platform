package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.time.Instant;

public record LocationHistoryResource(
        Double latitude,
        Double longitude,
        Double accuracyInMeters,
        String status,
        Instant recordedAt
) {}
