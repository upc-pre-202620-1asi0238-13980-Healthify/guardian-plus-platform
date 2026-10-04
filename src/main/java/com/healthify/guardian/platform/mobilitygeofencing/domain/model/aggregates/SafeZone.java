package com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.CreateSafeZoneCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;

import java.time.Instant;

public class SafeZone {
    private final SafeZoneId id;
    private final FragileCitizenId fragileCitizenId;
    private String name;
    private SafeZoneBoundary boundary;
    private SafeZoneStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public SafeZone(CreateSafeZoneCommand command) {
        this.id = SafeZoneId.generate();
        this.fragileCitizenId = new FragileCitizenId(command.fragileCitizenId());
        this.name = command.name();
        this.boundary = new SafeZoneBoundary(command.center(), command.radiusInMeters());
        this.status = SafeZoneStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public SafeZone(SafeZoneId id, FragileCitizenId fragileCitizenId, String name, SafeZoneBoundary boundary, SafeZoneStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.fragileCitizenId = fragileCitizenId;
        this.name = name;
        this.boundary = boundary;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateBoundary(SafeZoneBoundary boundary) {
        this.boundary = boundary;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.status = SafeZoneStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = SafeZoneStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }

    public boolean contains(Location location) {
        if (!isActive() || location == null) return false;
        return boundary.contains(location.coordinates());
    }

    public boolean isActive() { return status == SafeZoneStatus.ACTIVE; }

    // Getters
    public SafeZoneId getId() { return id; }
    public FragileCitizenId getFragileCitizenId() { return fragileCitizenId; }
    public String getName() { return name; }
    public SafeZoneBoundary getBoundary() { return boundary; }
    public SafeZoneStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}