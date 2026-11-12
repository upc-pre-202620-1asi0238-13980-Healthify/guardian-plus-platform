package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record CallSessionResource(
        UUID callId,
        UUID fragileCitizenId,
        String activeType,
        String status,
        boolean downgraded,
        String reasonMessage,
        Instant startedAt
) {}
