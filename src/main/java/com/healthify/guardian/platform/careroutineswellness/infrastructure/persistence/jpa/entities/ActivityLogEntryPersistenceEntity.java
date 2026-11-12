package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityLogEntryType;
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
 * JPA persistence entity for activity log entries.
 */
@Entity
@Table(name = "activity_log_entries")
@Getter
@Setter
@NoArgsConstructor
public class ActivityLogEntryPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Convert(converter = PersonUnderCareIdPersistenceConverter.class)
    @Column(name = "person_under_care_id", nullable = false)
    private PersonUnderCareId personUnderCareId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityLogEntryType type;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    private Integer steps;

    @Column(name = "inactive_minutes", precision = 10, scale = 1)
    private BigDecimal inactiveMinutes;
}
