package com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.LocationPoint;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;

public class SafeZone {

    private final SafeZoneId id;
    private final CareRecipientProfileId careRecipientProfileId;
    private String name;
    private LocationPoint centerPoint;
    private double radiusInMeters;
    private boolean active;

    public SafeZone(SafeZoneId id, CareRecipientProfileId careRecipientProfileId, String name, LocationPoint centerPoint, double radiusInMeters) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The name of the safe zone is mandatory.");
        }
        if (radiusInMeters <= 0) {
            throw new IllegalArgumentException("The radius must be greater than 0 meters.");
        }
        this.id = id != null ? id : SafeZoneId.generate();
        this.careRecipientProfileId = careRecipientProfileId;
        this.name = name;
        this.centerPoint = centerPoint;
        this.radiusInMeters = radiusInMeters;
        this.active = true;
    }

    /**
     * Evaluates whether a geographic point lies within the radius of this safe zone.
     */
    public boolean contains(LocationPoint point) {
        if (!active || point == null) {
            return false;
        }
        return centerPoint.distanceToInMeters(point) <= radiusInMeters;
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateDetails(String name, LocationPoint centerPoint, double radiusInMeters) {
        if (radiusInMeters <= 0) {
            throw new IllegalArgumentException("The radius must be greater than 0 meters.");
        }
        this.name = name;
        this.centerPoint = centerPoint;
        this.radiusInMeters = radiusInMeters;
    }

    // Getters
    public SafeZoneId getId() { return id; }
    public CareRecipientProfileId getCareRecipientProfileId() { return careRecipientProfileId; }
    public String getName() { return name; }
    public LocationPoint getCenterPoint() { return centerPoint; }
    public double getRadiusInMeters() { return radiusInMeters; }
    public boolean isActive() { return active; }
}
