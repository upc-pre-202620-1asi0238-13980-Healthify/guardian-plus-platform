package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record CurrentLocationResource(
        UUID fragileCitizenId,
        Double latitude,
        Double longitude,
        Double accuracyInMeters,
        String status,
        Instant recordedAt
) {}
