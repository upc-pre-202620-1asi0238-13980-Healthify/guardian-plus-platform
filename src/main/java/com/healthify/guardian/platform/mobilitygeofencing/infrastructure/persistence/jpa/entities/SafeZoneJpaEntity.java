package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "safe_zones")
public class SafeZoneJpaEntity {

    @Id
    private UUID id;

    @Column(name = "fragile_citizen_id", nullable = false)
    private UUID fragileCitizenId;

    @Column(nullable = false)
    private String name;

    @Column(name = "center_latitude", nullable = false)
    private Double centerLatitude;

    @Column(name = "center_longitude", nullable = false)
    private Double centerLongitude;

    @Column(name = "radius_in_meters", nullable = false)
    private Double radiusInMeters;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SafeZoneJpaEntity() {}

    public SafeZoneJpaEntity(UUID id, UUID fragileCitizenId, String name, Double centerLatitude, Double centerLongitude, Double radiusInMeters, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.fragileCitizenId = fragileCitizenId;
        this.name = name;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.radiusInMeters = radiusInMeters;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFragileCitizenId() { return fragileCitizenId; }
    public void setFragileCitizenId(UUID fragileCitizenId) { this.fragileCitizenId = fragileCitizenId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getCenterLatitude() { return centerLatitude; }
    public void setCenterLatitude(Double centerLatitude) { this.centerLatitude = centerLatitude; }
    public Double getCenterLongitude() { return centerLongitude; }
    public void setCenterLongitude(Double centerLongitude) { this.centerLongitude = centerLongitude; }
    public Double getRadiusInMeters() { return radiusInMeters; }
    public void setRadiusInMeters(Double radiusInMeters) { this.radiusInMeters = radiusInMeters; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}