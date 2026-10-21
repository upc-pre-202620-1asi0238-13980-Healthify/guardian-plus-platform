package com.healthify.guardian.platform.mobilitygeofencing.domain.model.events;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;

import java.time.Instant;

public record SafeZoneUpdatedEvent(SafeZoneId safeZoneId, Instant occurredAt) {
    public SafeZoneUpdatedEvent(SafeZoneId safeZoneId) { this(safeZoneId, Instant.now()); }
}
