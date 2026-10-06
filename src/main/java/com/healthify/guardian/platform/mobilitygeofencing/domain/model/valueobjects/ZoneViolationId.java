package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.util.UUID;

public record ZoneViolationId(UUID value) {
    public ZoneViolationId { if (value == null) throw new IllegalArgumentException("ZoneViolationId cannot be null."); }
    public static ZoneViolationId generate() { return new ZoneViolationId(UUID.randomUUID()); }
}
