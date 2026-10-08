package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RecurrenceFrequency;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
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

import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence entity for reminders. One row per occurrence of a (possibly recurring) reminder.
 *
 * <p>Columns added after the first release are nullable so {@code ddl-auto=update} can add them to a table
 * that already has rows; the persistence assembler fills in the defaults for those legacy rows.</p>
 */
@Entity
@Table(name = "reminders")
@Getter
@Setter
@NoArgsConstructor
public class ReminderPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "series_id")
    private UUID seriesId;

    @Convert(converter = PersonUnderCareIdPersistenceConverter.class)
    @Column(name = "person_under_care_id", nullable = false)
    private PersonUnderCareId personUnderCareId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType type;

    private String title;

    private String dosage;

    @Column(length = 500)
    private String instructions;

    private String location;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "scheduled_time", nullable = false)
    private Instant scheduledTime;

    @Column(name = "lead_time_minutes")
    private Integer leadTimeMinutes;

    /** Denormalized {@code scheduledTime - leadTime}, so the due-check query can filter on it. */
    @Column(name = "notify_at")
    private Instant notifyAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_frequency")
    private RecurrenceFrequency recurrenceFrequency;

    /** Comma-separated {@code DayOfWeek} names, only for weekly reminders. */
    @Column(name = "recurrence_days_of_week")
    private String recurrenceDaysOfWeek;

    @Column(name = "recurrence_interval_hours")
    private Integer recurrenceIntervalHours;

    @Column(name = "medication_stock_id")
    private UUID medicationStockId;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status;

    @Column(name = "reissue_count", nullable = false)
    private Integer reissueCount;
}
