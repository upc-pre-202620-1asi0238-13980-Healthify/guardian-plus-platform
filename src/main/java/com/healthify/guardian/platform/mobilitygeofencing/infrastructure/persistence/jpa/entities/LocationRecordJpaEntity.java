package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "location_records")
public class LocationRecordJpaEntity {

    @Id
    private UUID id;

    @Column(name = "location_tracking_id", nullable = false)
    private UUID locationTrackingId;

    @Column(name = "fragile_citizen_id", nullable = false)
    private UUID fragileCitizenId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "accuracy_in_meters")
    private Double accuracyInMeters;

    private String status;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public LocationRecordJpaEntity() {}

    public LocationRecordJpaEntity(UUID id, UUID locationTrackingId, UUID fragileCitizenId, Double latitude, Double longitude, Double accuracyInMeters, String status, Instant recordedAt) {
        this.id = id;
        this.locationTrackingId = locationTrackingId;
        this.fragileCitizenId = fragileCitizenId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyInMeters = accuracyInMeters;
        this.status = status;
        this.recordedAt = recordedAt;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public UUID getLocationTrackingId() { return locationTrackingId; }
    public UUID getFragileCitizenId() { return fragileCitizenId; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getAccuracyInMeters() { return accuracyInMeters; }
    public String getStatus() { return status; }
    public Instant getRecordedAt() { return recordedAt; }
}
