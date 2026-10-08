package com.healthify.guardian.platform.mobilitygeofencing.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record WearableLocationMessage(
        UUID fragileCitizenId,
        Double latitude,
        Double longitude,
        Double accuracyInMeters,
        Instant recordedAt
) {}
