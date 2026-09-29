package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderCancelledEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderConfirmedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderIssuedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderReissuedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderScheduledEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderSuppressedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.IssuanceOutcome;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;

/**
 * Aggregate root representing a single routine reminder (medication, medical appointment,
 * physical activity or hydration) and its full lifecycle.
 *
 * <p>Protects valid state transitions but delegates the decision of <em>when</em> to issue,
 * suppress or reissue to {@code ReminderIssuancePolicy} and {@code ReminderReissuePolicy};
 * this aggregate only applies whatever outcome those policies hand back.</p>
 */
@Getter
public class Reminder extends AbstractDomainAggregateRoot<Reminder> {

    private static final Set<ReminderStatus> ISSUABLE_FROM = Set.of(ReminderStatus.SCHEDULED);
    private static final Set<ReminderStatus> CONFIRMABLE_FROM = Set.of(ReminderStatus.ISSUED, ReminderStatus.REISSUED);
    private static final Set<ReminderStatus> CANCELLABLE_FROM =
            Set.of(ReminderStatus.SCHEDULED, ReminderStatus.ISSUED, ReminderStatus.REISSUED);
    private static final Set<ReminderStatus> REISSUABLE_FROM = Set.of(ReminderStatus.ISSUED);
    private static final Set<ReminderStatus> ACTIVE_STATUSES =
            Set.of(ReminderStatus.SCHEDULED, ReminderStatus.ISSUED, ReminderStatus.REISSUED);

    private ReminderId id;
    private PersonUnderCareId personUnderCareId;
    private ReminderType type;
    private Instant scheduledTime;
    private Instant issuedAt;
    private ReminderStatus status;
    private Integer reissueCount;

    /** Reconstitution constructor, used by the persistence assembler. */
    public Reminder() {
    }

    /** Creates a new reminder in {@code SCHEDULED} status from a scheduling command. */
    public Reminder(ScheduleReminderCommand command) {
        this.id = ReminderId.generate();
        this.personUnderCareId = new PersonUnderCareId(command.personUnderCareId());
        this.type = command.type();
        this.scheduledTime = command.scheduledTime();
        this.status = ReminderStatus.SCHEDULED;
        this.reissueCount = 0;
        registerDomainEvent(ReminderScheduledEvent.from(this));
    }

    /**
     * Applies the outcome decided by {@code ReminderIssuancePolicy} once this reminder's
     * scheduled time is due.
     *
     * @param outcome     whether to issue or suppress the reminder
     * @param currentTime the time the outcome is being applied at
     */
    public void issue(IssuanceOutcome outcome, Instant currentTime) {
        if (!ISSUABLE_FROM.contains(status)) {
            throw new IllegalStateException("reminder.cannot.issue");
        }
        if (outcome == IssuanceOutcome.SUPPRESS) {
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
        registerDomainEvent(ReminderConfirmedEvent.from(this, Instant.now()));
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
     * Reissues this reminder because {@code ReminderReissuePolicy} determined it was not
     * confirmed within the tolerance window.
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

    /** True while this reminder can still transition (i.e. it is not in a terminal status). */
    public boolean isActive() {
        return ACTIVE_STATUSES.contains(status);
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(ReminderId id) {
        this.id = id;
    }

    public void setPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        this.personUnderCareId = personUnderCareId;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public void setStatus(ReminderStatus status) {
        this.status = status;
    }

    public void setReissueCount(Integer reissueCount) {
        this.reissueCount = reissueCount;
    }
}
