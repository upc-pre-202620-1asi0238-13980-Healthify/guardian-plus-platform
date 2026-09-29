package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ActivityResumedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ProlongedInactivityDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityMonitorId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root tracking the physical-activity status of a person under care over time.
 *
 * <p>Exactly one monitor exists per person under care; it is created lazily the first time
 * activity telemetry is received for that person.</p>
 */
@Getter
public class ActivityMonitor extends AbstractDomainAggregateRoot<ActivityMonitor> {

    private ActivityMonitorId id;
    private PersonUnderCareId personUnderCareId;
    private ActivityStatus status;
    private Instant inactivitySince;

    /** Reconstitution constructor, used by the persistence assembler. */
    public ActivityMonitor() {
    }

    /** Creates a fresh monitor in {@code NORMAL} status for a person under care. */
    public ActivityMonitor(PersonUnderCareId personUnderCareId) {
        this.id = ActivityMonitorId.generate();
        this.personUnderCareId = personUnderCareId;
        this.status = ActivityStatus.NORMAL;
    }

    /**
     * Records that the wearable's kinematic sensors detected prolonged physical inactivity.
     * Idempotent: if inactivity was already detected, this call has no effect.
     *
     * @param detectedAt when the prolonged inactivity was detected
     */
    public void recordProlongedInactivity(Instant detectedAt) {
        if (status == ActivityStatus.INACTIVITY_DETECTED) {
            return;
        }
        this.status = ActivityStatus.INACTIVITY_DETECTED;
        this.inactivitySince = detectedAt;
        registerDomainEvent(ProlongedInactivityDetectedEvent.from(this));
    }

    /**
     * Records that activity resumed after a prolonged-inactivity episode.
     * Idempotent: if the monitor is already normal, this call has no effect.
     *
     * @param resumedAt when activity resumed
     */
    public void recordActivityResumed(Instant resumedAt) {
        if (status == ActivityStatus.NORMAL) {
            return;
        }
        this.status = ActivityStatus.NORMAL;
        this.inactivitySince = null;
        registerDomainEvent(ActivityResumedEvent.from(this, resumedAt));
    }

    /** True while this monitor is currently flagging prolonged inactivity. */
    public boolean isInactive() {
        return status == ActivityStatus.INACTIVITY_DETECTED;
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(ActivityMonitorId id) {
        this.id = id;
    }

    public void setPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        this.personUnderCareId = personUnderCareId;
    }

    public void setStatus(ActivityStatus status) {
        this.status = status;
    }

    public void setInactivitySince(Instant inactivitySince) {
        this.inactivitySince = inactivitySince;
    }
}
