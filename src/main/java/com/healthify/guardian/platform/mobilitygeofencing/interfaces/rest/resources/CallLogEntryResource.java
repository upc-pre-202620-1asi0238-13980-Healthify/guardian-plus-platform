package com.healthify.guardian.platform.mobilitygeofencing.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record CallLogEntryResource(
        UUID id,
        UUID fragileCitizenId,
        String callType,
        String status,
        boolean downgraded,
        String reason,
        Long durationSeconds,
        Instant occurredAt
) {}
