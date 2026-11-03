package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentStatus;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for incidents. One row per acknowledged alert at most.
 */
@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
public class IncidentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "alert_id", nullable = false, unique = true, updatable = false)
    private UUID alertId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status;

    @Column(name = "marked_in_attention_at", nullable = false)
    private Instant markedInAttentionAt;

    @Column(name = "stabilized_at")
    private Instant stabilizedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
