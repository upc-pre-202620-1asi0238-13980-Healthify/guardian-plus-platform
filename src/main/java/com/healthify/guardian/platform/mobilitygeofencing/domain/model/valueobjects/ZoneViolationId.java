package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.util.UUID;

public record ZoneViolationId(UUID value) {
    public ZoneViolationId { if (value == null) throw new IllegalArgumentException("ZoneViolationId no puede ser nulo."); }
    public static ZoneViolationId generate() { return new ZoneViolationId(UUID.randomUUID()); }
}
