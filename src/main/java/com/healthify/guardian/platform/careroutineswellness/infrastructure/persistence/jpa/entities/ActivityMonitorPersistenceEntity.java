package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.converters.PersonUnderCareIdPersistenceConverter;
import com.healthify.guardian.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA persistence entity for activity monitors. One row per person under care.
 *
 * <p>Columns added after the first release are nullable so {@code ddl-auto=update} can add them to a table
 * that already has rows; the persistence assembler fills in the defaults for those legacy rows.</p>
 */
@Entity
@Table(name = "activity_monitors")
@Getter
@Setter
@NoArgsConstructor
public class ActivityMonitorPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = PersonUnderCareIdPersistenceConverter.class)
    @Column(name = "person_under_care_id", nullable = false, unique = true)
    private PersonUnderCareId personUnderCareId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    @Column(name = "inactivity_since")
    private Instant inactivitySince;

    @Column(name = "detection_enabled")
    private Boolean detectionEnabled;

    @Column(name = "inactivity_threshold_minutes")
    private Integer inactivityThresholdMinutes;

    @Column(name = "inactive_minutes", precision = 10, scale = 1)
    private BigDecimal inactiveMinutes;

    @Column(name = "last_movement_at")
    private Instant lastMovementAt;

    @Column(name = "last_sample_at")
    private Instant lastSampleAt;

    @Column(name = "active_since")
    private Instant activeSince;

    @Column(name = "active_streak_steps")
    private Integer activeStreakSteps;
}
