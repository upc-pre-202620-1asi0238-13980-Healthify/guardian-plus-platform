package com.healthify.guardian.platform.mobilitygeofencing.domain.model.events;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.LocationPoint;

import java.time.Instant;

public record SafeZoneViolationEvent(
        CareRecipientProfileId careRecipientProfileId,
        LocationPoint lastLocationPoint,
        Instant occurredAt
) {
    public SafeZoneViolationEvent(CareRecipientProfileId careRecipientProfileId, LocationPoint lastLocationPoint) {
        this(careRecipientProfileId, lastLocationPoint, Instant.now());
    }
}