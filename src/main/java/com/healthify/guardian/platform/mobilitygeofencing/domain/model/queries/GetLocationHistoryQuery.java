package com.healthify.guardian.platform.mobilitygeofencing.domain.model.queries;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;

import java.time.Instant;


public record GetLocationHistoryQuery(
        FragileCitizenId fragileCitizenId,
        Instant start,
        Instant end
) {
    public GetLocationHistoryQuery {
        if (fragileCitizenId == null) throw new IllegalArgumentException("fragileCitizenId cannot be null.");
    }
}
