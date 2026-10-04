package com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects;

import java.util.UUID;

public record LocationTrackingId(UUID value) {
    public LocationTrackingId { if (value == null) throw new IllegalArgumentException("LocationTrackingId cannot be null."); }
    public static LocationTrackingId generate() { return new LocationTrackingId(UUID.randomUUID()); }
}
