package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MovementDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ProlongedInactivityDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.WalkDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityLogEntryId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityLogEntryType;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Aggregate root for one immutable, noteworthy activity fact of a person under care (movement after a long
 * pause, a finished walk or a prolonged-inactivity detection), kept so the family member can see the
 * person's recent activity.
 *
 * <p>Entries are written from the {@code ActivityMonitor}'s domain events instead of being kept inside that
 * aggregate, so the monitor stays small no matter how long the log grows.</p>
 */
@Getter
public class ActivityLogEntry extends AbstractDomainAggregateRoot<ActivityLogEntry> {

    private ActivityLogEntryId id;
    private PersonUnderCareId personUnderCareId;
    private ActivityLogEntryType type;
    private Instant occurredAt;
    private Integer durationMinutes;
    private Integer steps;
    private BigDecimal inactiveMinutes;

    /** Reconstitution constructor, used by the persistence assembler. */
    public ActivityLogEntry() {
    }

    private ActivityLogEntry(PersonUnderCareId personUnderCareId, ActivityLogEntryType type, Instant occurredAt) {
        this.id = ActivityLogEntryId.generate();
        this.personUnderCareId = personUnderCareId;
        this.type = type;
        this.occurredAt = occurredAt;
    }

    /** Logs that the person moved again after a long still period. */
    public static ActivityLogEntry from(MovementDetectedEvent event) {
        var entry = new ActivityLogEntry(event.personUnderCareId(), ActivityLogEntryType.MOVEMENT_DETECTED, event.detectedAt());
        entry.inactiveMinutes = event.previousInactiveMinutes();
        return entry;
    }

    /** Logs a finished walk, with its duration and steps. */
    public static ActivityLogEntry from(WalkDetectedEvent event) {
        var entry = new ActivityLogEntry(event.personUnderCareId(), ActivityLogEntryType.WALK_DETECTED, event.startedAt());
        entry.durationMinutes = (int) Duration.between(event.startedAt(), event.endedAt()).toMinutes();
        entry.steps = event.steps();
        return entry;
    }

    /** Logs that prolonged inactivity was detected. */
    public static ActivityLogEntry from(ProlongedInactivityDetectedEvent event) {
        var entry = new ActivityLogEntry(
                event.personUnderCareId(), ActivityLogEntryType.PROLONGED_INACTIVITY_DETECTED, event.detectedAt());
        entry.inactiveMinutes = event.inactiveMinutes();
        return entry;
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(ActivityLogEntryId id) {
        this.id = id;
    }

    public void setPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        this.personUnderCareId = personUnderCareId;
    }

    public void setType(ActivityLogEntryType type) {
        this.type = type;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public void setInactiveMinutes(BigDecimal inactiveMinutes) {
        this.inactiveMinutes = inactiveMinutes;
    }
}
