package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "location_trackings")
public class LocationTrackingJpaEntity {

    @Id
    private UUID id;

    @Column(name = "fragile_citizen_id", nullable = false, unique = true)
    private UUID fragileCitizenId;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;

    @Column(name = "accuracy_in_meters")
    private Double accuracyInMeters;

    @Column(name = "current_status")
    private String currentStatus;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedAt;

    public LocationTrackingJpaEntity() {}

    public LocationTrackingJpaEntity(UUID id, UUID fragileCitizenId, Double currentLatitude, Double currentLongitude, Double accuracyInMeters, String currentStatus, Instant lastUpdatedAt) {
        this.id = id;
        this.fragileCitizenId = fragileCitizenId;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.accuracyInMeters = accuracyInMeters;
        this.currentStatus = currentStatus;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFragileCitizenId() { return fragileCitizenId; }
    public void setFragileCitizenId(UUID fragileCitizenId) { this.fragileCitizenId = fragileCitizenId; }
    public Double getCurrentLatitude() { return currentLatitude; }
    public void setCurrentLatitude(Double currentLatitude) { this.currentLatitude = currentLatitude; }
    public Double getCurrentLongitude() { return currentLongitude; }
    public void setCurrentLongitude(Double currentLongitude) { this.currentLongitude = currentLongitude; }
    public Double getAccuracyInMeters() { return accuracyInMeters; }
    public void setAccuracyInMeters(Double accuracyInMeters) { this.accuracyInMeters = accuracyInMeters; }
    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(Instant lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
}
