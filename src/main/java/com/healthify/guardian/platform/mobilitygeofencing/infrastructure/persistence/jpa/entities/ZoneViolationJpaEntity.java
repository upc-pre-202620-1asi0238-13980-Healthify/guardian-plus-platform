package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "zone_violations")
public class ZoneViolationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "safe_zone_id", nullable = false)
    private UUID safeZoneId;

    @Column(name = "fragile_citizen_id", nullable = false)
    private UUID fragileCitizenId;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "accuracy_in_meters")
    private Double accuracyInMeters;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    public ZoneViolationJpaEntity() {}

    public ZoneViolationJpaEntity(UUID id, UUID safeZoneId, UUID fragileCitizenId, Double latitude, Double longitude, Double accuracyInMeters, Instant detectedAt) {
        this.id = id;
        this.safeZoneId = safeZoneId;
        this.fragileCitizenId = fragileCitizenId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyInMeters = accuracyInMeters;
        this.detectedAt = detectedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getSafeZoneId() { return safeZoneId; }
    public UUID getFragileCitizenId() { return fragileCitizenId; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getAccuracyInMeters() { return accuracyInMeters; }
    public Instant getDetectedAt() { return detectedAt; }
}