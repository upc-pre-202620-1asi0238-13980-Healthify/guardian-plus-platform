package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "safe_zones")
public class SafeZoneJpaEntity {

    @Id
    private UUID id;

    @Column(name = "care_recipient_profile_id", nullable = false)
    private UUID careRecipientProfileId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "radius_in_meters", nullable = false)
    private Double radiusInMeters;

    @Column(nullable = false)
    private Boolean active;

    public SafeZoneJpaEntity() {}

    public SafeZoneJpaEntity(UUID id, UUID careRecipientProfileId, String name, Double latitude, Double longitude, Double radiusInMeters, Boolean active) {
        this.id = id;
        this.careRecipientProfileId = careRecipientProfileId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusInMeters = radiusInMeters;
        this.active = active;
    }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCareRecipientProfileId() { return careRecipientProfileId; }
    public void setCareRecipientProfileId(UUID careRecipientProfileId) { this.careRecipientProfileId = careRecipientProfileId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getRadiusInMeters() { return radiusInMeters; }
    public void setRadiusInMeters(Double radiusInMeters) { this.radiusInMeters = radiusInMeters; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
