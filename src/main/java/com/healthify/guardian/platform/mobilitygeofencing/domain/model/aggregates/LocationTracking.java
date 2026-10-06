package com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;

import java.time.Instant;

public class LocationTracking {
    private final LocationTrackingId id;
    private final FragileCitizenId fragileCitizenId;
    private Location currentLocation;
    private LocationStatus currentStatus;
    private Instant lastUpdatedAt;

    public LocationTracking(FragileCitizenId fragileCitizenId) {
        this.id = LocationTrackingId.generate();
        this.fragileCitizenId = fragileCitizenId;
        this.lastUpdatedAt = Instant.now();
    }

    public LocationTracking(LocationTrackingId id, FragileCitizenId fragileCitizenId, Location currentLocation, LocationStatus currentStatus, Instant lastUpdatedAt) {
        this.id = id;
        this.fragileCitizenId = fragileCitizenId;
        this.currentLocation = currentLocation;
        this.currentStatus = currentStatus;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public void recordLocation(Location location, LocationStatus status) {
        this.currentLocation = location;
        this.currentStatus = status;
        this.lastUpdatedAt = Instant.now();
    }

    public LocationTrackingId getId() { return id; }
    public FragileCitizenId getFragileCitizenId() { return fragileCitizenId; }
    public Location getCurrentLocation() { return currentLocation; }
    public LocationStatus getCurrentStatus() { return currentStatus; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
}
