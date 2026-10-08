package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderCancelledEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderConfirmedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderIssuedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderMissedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderReissuedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderScheduledEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderSuppressedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RecurrenceRule;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReissueTolerance;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderDetails;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderSeriesId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

/**
 * Aggregate root representing a single occurrence of a routine reminder (medication, medical
 * appointment, physical activity or hydration) and its full lifecycle.
 *
 * <p>Protects valid state transitions and owns the rules that decide whether a due reminder is issued or
 * suppressed and whether an unconfirmed one must be reissued. <em>When</em> those rules are applied is up to
 * the Reminder Issuance and Reminder Reissue policies, run periodically by the schedulers.</p>
 *
 * <p>A recurring routine is a series of occurrences sharing one {@link ReminderSeriesId}: once an
 * occurrence leaves the {@code SCHEDULED} status, {@link #nextOccurrence} builds the following one.
 * Keeping one aggregate per occurrence is what lets adherence be measured dose by dose.</p>
 */
@Getter
public class Reminder extends AbstractDomainAggregateRoot<Reminder> {

    private static final Set<ReminderStatus> ISSUABLE_FROM = Set.of(ReminderStatus.SCHEDULED);
    private static final Set<ReminderStatus> CONFIRMABLE_FROM = Set.of(ReminderStatus.ISSUED, ReminderStatus.REISSUED);
    private static final Set<ReminderStatus> CANCELLABLE_FROM =
            Set.of(ReminderStatus.SCHEDULED, ReminderStatus.ISSUED, ReminderStatus.REISSUED);
    private static final Set<ReminderStatus> REISSUABLE_FROM = Set.of(ReminderStatus.ISSUED);
    private static final Set<ReminderStatus> MISSABLE_FROM = Set.of(ReminderStatus.ISSUED, ReminderStatus.REISSUED);
    private static final Set<ReminderStatus> ACTIVE_STATUSES =
            Set.of(ReminderStatus.SCHEDULED, ReminderStatus.ISSUED, ReminderStatus.REISSUED);
    private static final Set<ReminderStatus> REACHED_PERSON_STATUSES = Set.of(
            ReminderStatus.ISSUED, ReminderStatus.REISSUED, ReminderStatus.CONFIRMED, ReminderStatus.MISSED);

    private static final String SCHEDULED_TIME_BLANK_MESSAGE_KEY = "reminder.scheduled-time.blank";
    private static final String TYPE_BLANK_MESSAGE_KEY = "reminder.type.blank";
    private static final String LEAD_TIME_INVALID_MESSAGE_KEY = "reminder.lead-time-minutes.invalid";
    private static final String STOCK_ONLY_FOR_MEDICATION_MESSAGE_KEY = "reminder.medication-stock-id.not-medication";

    private ReminderId id;
    private ReminderSeriesId seriesId;
    private PersonUnderCareId personUnderCareId;
    private ReminderType type;
    private ReminderDetails details;
    private Instant scheduledTime;
    private Integer leadTimeMinutes;
    private RecurrenceRule recurrence;
    private MedicationStockId medicationStockId;
    private Instant issuedAt;
    private Instant confirmedAt;
    private ReminderStatus status;
    private Integer reissueCount;

    /** Reconstitution constructor, used by the persistence assembler. */
    public Reminder() {
    }

    /** Creates the first occurrence of a reminder, in {@code SCHEDULED} status, from a scheduling command. */
    public Reminder(ScheduleReminderCommand command) {
        if (command.type() == null) {
            throw new IllegalArgumentException(TYPE_BLANK_MESSAGE_KEY);
        }
        if (command.scheduledTime() == null) {
            throw new IllegalArgumentException(SCHEDULED_TIME_BLANK_MESSAGE_KEY);
        }
        var leadTime = command.leadTimeMinutes() == null ? 0 : command.leadTimeMinutes();
        if (leadTime < 0) {
            throw new IllegalArgumentException(LEAD_TIME_INVALID_MESSAGE_KEY);
        }
        if (command.medicationStockId() != null && command.type() != ReminderType.MEDICATION) {
            throw new IllegalArgumentException(STOCK_ONLY_FOR_MEDICATION_MESSAGE_KEY);
        }
        this.id = ReminderId.generate();
        this.seriesId = ReminderSeriesId.generate();
        this.personUnderCareId = new PersonUnderCareId(command.personUnderCareId());
        this.type = command.type();
        this.details = new ReminderDetails(command.title(), command.dosage(), command.instructions(),
                command.location(), command.durationMinutes());
        this.scheduledTime = command.scheduledTime();
        this.leadTimeMinutes = leadTime;
        this.recurrence = new RecurrenceRule(command.recurrenceFrequency(), command.daysOfWeek(), command.intervalHours());
        this.medicationStockId = command.medicationStockId() == null ? null : new MedicationStockId(command.medicationStockId());
        this.status = ReminderStatus.SCHEDULED;
        this.reissueCount = 0;
        registerDomainEvent(ReminderScheduledEvent.from(this));
    }

    /** Creates the occurrence that follows {@code previous} in the same series, due at {@code scheduledTime}. */
    private Reminder(Reminder previous, Instant scheduledTime) {
        this.id = ReminderId.generate();
        this.seriesId = previous.seriesId;
        this.personUnderCareId = previous.personUnderCareId;
        this.type = previous.type;
        this.details = previous.details;
        this.scheduledTime = scheduledTime;
        this.leadTimeMinutes = previous.leadTimeMinutes;
        this.recurrence = previous.recurrence;
        this.medicationStockId = previous.medicationStockId;
        this.status = ReminderStatus.SCHEDULED;
        this.reissueCount = 0;
        registerDomainEvent(ReminderScheduledEvent.from(this));
    }

    /**
     * When the reminder must reach the person under care: the routine's scheduled time minus the lead
     * time (e.g. one hour before a medical appointment).
     */
    public Instant notifyAt() {
        return scheduledTime.minus(Duration.ofMinutes(leadTimeMinutes == null ? 0 : leadTimeMinutes));
    }

    /**
     * Issues this reminder once its notification time is due, or suppresses it instead.
     *
     * <p>Business rule: a hydration reminder that falls inside the person's sleep window is suppressed rather
     * than issued, to avoid waking them up for water, unless their hydration plan opted out of respecting the
     * sleep window. Every other reminder is issued.</p>
     *
     * @param currentTime        the time the reminder is being issued at
     * @param sleepWindow        the person's sleep window
     * @param zone               the zone the sleep window is expressed in
     * @param respectSleepWindow whether the person's hydration plan respects the sleep window
     */
    public void issue(Instant currentTime, SleepWindow sleepWindow, ZoneId zone, boolean respectSleepWindow) {
        if (!ISSUABLE_FROM.contains(status)) {
            throw new IllegalStateException("reminder.cannot.issue");
        }
        if (type == ReminderType.HYDRATION && respectSleepWindow && sleepWindow.contains(currentTime, zone)) {
            this.status = ReminderStatus.SUPPRESSED;
            registerDomainEvent(ReminderSuppressedEvent.from(this, currentTime));
            return;
        }
        this.status = ReminderStatus.ISSUED;
        this.issuedAt = currentTime;
        registerDomainEvent(ReminderIssuedEvent.from(this));
    }

    /** Confirms this reminder once the person under care has acknowledged it. */
    public void confirm() {
        if (!CONFIRMABLE_FROM.contains(status)) {
            throw new IllegalStateException("reminder.cannot.confirm");
        }
        this.status = ReminderStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
        registerDomainEvent(ReminderConfirmedEvent.from(this));
    }

    /** Cancels this reminder before it has been confirmed. */
    public void cancel() {
        if (!CANCELLABLE_FROM.contains(status)) {
            throw new IllegalStateException("reminder.cannot.cancel");
        }
        this.status = ReminderStatus.CANCELLED;
        registerDomainEvent(ReminderCancelledEvent.from(this, Instant.now()));
    }

    /**
     * Tells whether this reminder must be reissued because the person has not confirmed it in time.
     *
     * <p>Business rule: only medication reminders are reissued, once, when they have been waiting for
     * confirmation for at least the tolerance; any other issued reminder is left to lapse.</p>
     *
     * @param currentTime the current time
     * @param tolerance   how long an issued reminder waits for confirmation
     * @return true if the reminder is an issued medication reminder overdue for confirmation
     */
    public boolean isOverdueForReissue(Instant currentTime, ReissueTolerance tolerance) {
        return type == ReminderType.MEDICATION
                && status == ReminderStatus.ISSUED
                && issuedAt != null
                && !issuedAt.plus(tolerance.duration()).isAfter(currentTime);
    }

    /**
     * Reissues this reminder because it was not confirmed within the tolerance window
     * (see {@link #isOverdueForReissue}).
     */
    public void reissue() {
        if (!REISSUABLE_FROM.contains(status)) {
            throw new IllegalStateException("reminder.cannot.reissue");
        }
        this.status = ReminderStatus.REISSUED;
        this.issuedAt = Instant.now();
        this.reissueCount = this.reissueCount + 1;
        registerDomainEvent(ReminderReissuedEvent.from(this));
    }

    /**
     * Closes an issued (or reissued) reminder that the person under care never confirmed, once the family
     * member has reviewed the omission. No dose is discounted from the medication stock.
     */
    public void markAsMissed() {
        if (!MISSABLE_FROM.contains(status)) {
            throw new IllegalStateException("reminder.cannot.miss");
        }
        this.status = ReminderStatus.MISSED;
        registerDomainEvent(ReminderMissedEvent.from(this, Instant.now()));
    }

    /**
     * Builds the next occurrence of this reminder's series, if the reminder is recurring.
     *
     * @param currentTime the current time; the next occurrence is never scheduled in the past
     * @param zone        zone the local time of day of daily and weekly series is kept in
     * @return the next occurrence, ready to be saved, or empty for a one-off reminder
     */
    public Optional<Reminder> nextOccurrence(Instant currentTime, ZoneId zone) {
        return recurrence.nextOccurrenceAfter(scheduledTime, currentTime, zone)
                .map(nextScheduledTime -> new Reminder(this, nextScheduledTime));
    }

    /**
     * True when this reminder actually reached the person under care (issued, reissued, confirmed or
     * missed), so it counts towards their adherence. Reminders still scheduled, cancelled by the family or
     * suppressed during the sleep window are not the person's responsibility.
     */
    public boolean hasReachedPerson() {
        return REACHED_PERSON_STATUSES.contains(status);
    }

    /** True while this reminder can still transition (i.e. it is not in a terminal status). */
    public boolean isActive() {
        return ACTIVE_STATUSES.contains(status);
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(ReminderId id) {
        this.id = id;
    }

    public void setSeriesId(ReminderSeriesId seriesId) {
        this.seriesId = seriesId;
    }

    public void setPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        this.personUnderCareId = personUnderCareId;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public void setDetails(ReminderDetails details) {
        this.details = details;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setLeadTimeMinutes(Integer leadTimeMinutes) {
        this.leadTimeMinutes = leadTimeMinutes;
    }

    public void setRecurrence(RecurrenceRule recurrence) {
        this.recurrence = recurrence;
    }

    public void setMedicationStockId(MedicationStockId medicationStockId) {
        this.medicationStockId = medicationStockId;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public void setReissueCount(Integer reissueCount) {
        this.reissueCount = reissueCount;
    }
}
