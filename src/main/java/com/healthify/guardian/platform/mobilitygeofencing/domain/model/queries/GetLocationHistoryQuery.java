package com.healthify.guardian.platform.mobilitygeofencing.domain.model.queries;


import java.time.Instant;
import java.util.UUID;

public record GetLocationHistoryQuery(
        UUID fragileCitizenId,
        Instant periodStart,
        Instant periodEnd
) {}
