package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.util.UUID;

public record LocationTrackingId(UUID value) {
    public LocationTrackingId { if (value == null) throw new IllegalArgumentException("LocationTrackingId no puede ser nulo."); }
    public static LocationTrackingId generate() { return new LocationTrackingId(UUID.randomUUID()); }
}
