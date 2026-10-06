package com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;

import java.time.Instant;

public class ZoneViolation {
    private final ZoneViolationId id;
    private final SafeZoneId safeZoneId;
    private final FragileCitizenId fragileCitizenId;
    private final Location location;
    private final Instant detectedAt;

    public ZoneViolation(SafeZoneId safeZoneId, FragileCitizenId fragileCitizenId, Location location) {
        this.id = ZoneViolationId.generate();
        this.safeZoneId = safeZoneId;
        this.fragileCitizenId = fragileCitizenId;
        this.location = location;
        this.detectedAt = Instant.now();
    }

    public ZoneViolation(ZoneViolationId id, SafeZoneId safeZoneId, FragileCitizenId fragileCitizenId, Location location, Instant detectedAt) {
        this.id = id;
        this.safeZoneId = safeZoneId;
        this.fragileCitizenId = fragileCitizenId;
        this.location = location;
        this.detectedAt = detectedAt;
    }

    public ZoneViolationId getId() { return id; }
    public SafeZoneId getSafeZoneId() { return safeZoneId; }
    public FragileCitizenId getFragileCitizenId() { return fragileCitizenId; }
    public Location getLocation() { return location; }
    public Instant getDetectedAt() { return detectedAt; }
}