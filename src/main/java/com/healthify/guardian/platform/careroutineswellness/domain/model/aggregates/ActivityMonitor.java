package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ActivityResumedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MovementDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ProlongedInactivityDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.WalkDetectedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityMonitorId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivitySample;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityStatus;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.InactivityDetectionSettings;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Aggregate root tracking the physical-activity status of a person under care over time.
 *
 * <p>Exactly one monitor exists per person under care; it is created lazily the first time
 * activity telemetry (or its configuration) is received for that person.</p>
 *
 * <p>The monitor owns the prolonged-inactivity rule: the wearable only reports how long the person has
 * been still, and this aggregate decides, with the person's own {@link InactivityDetectionSettings},
 * whether that is worth an alert. Detection only applies during the watch hours, i.e. outside the
 * person's sleep window.</p>
 */
@Getter
public class ActivityMonitor extends AbstractDomainAggregateRoot<ActivityMonitor> {

    /** Movement after at least this long still is logged as "counter reset", shorter pauses are not noteworthy. */
    static final int MOVEMENT_LOG_MIN_INACTIVE_MINUTES = 15;
    /** A burst of movement with at least this many steps is logged as a walk. */
    static final int WALK_MIN_STEPS = 150;

    private ActivityMonitorId id;
    private PersonUnderCareId personUnderCareId;
    private ActivityStatus status;
    private Instant inactivitySince;
    private InactivityDetectionSettings detectionSettings;
    private BigDecimal inactiveMinutes;
    private Instant lastMovementAt;
    private Instant lastSampleAt;
    private Instant activeSince;
    private Integer activeStreakSteps;

    /** Reconstitution constructor, used by the persistence assembler. */
    public ActivityMonitor() {
    }

    /** Creates a fresh monitor in {@code NORMAL} status, with the default detection settings. */
    public ActivityMonitor(PersonUnderCareId personUnderCareId) {
        this.id = ActivityMonitorId.generate();
        this.personUnderCareId = personUnderCareId;
        this.status = ActivityStatus.NORMAL;
        this.detectionSettings = InactivityDetectionSettings.defaults();
        this.inactiveMinutes = BigDecimal.ZERO;
        this.activeStreakSteps = 0;
    }

    /**
     * Applies a periodic sample of the wearable's kinematic sensors. Samples older than the last one applied
     * are ignored, since the broker does not guarantee ordering.
     *
     * @param sample      the sample reported by the wearable
     * @param sleepWindow the person's sleep window; inactivity is only watched outside of it
     * @param zone        the zone the sleep window is expressed in
     */
    public void recordSample(ActivitySample sample, SleepWindow sleepWindow, ZoneId zone) {
        if (lastSampleAt != null && sample.measuredAt().isBefore(lastSampleAt)) {
            return;
        }
        this.lastSampleAt = sample.measuredAt();
        if (sample.isMoving()) {
            onMovement(sample);
        } else {
            onStillness(sample, sleepWindow, zone);
        }
    }

    /**
     * Changes how prolonged inactivity is detected for this person.
     *
     * @param settings the new detection settings
     */
    public void configureDetection(InactivityDetectionSettings settings) {
        this.detectionSettings = settings;
    }

    /** True while this monitor is currently flagging prolonged inactivity. */
    public boolean isInactive() {
        return status == ActivityStatus.INACTIVITY_DETECTED;
    }

    private void onMovement(ActivitySample sample) {
        var stillFor = inactiveMinutes == null ? BigDecimal.ZERO : inactiveMinutes;
        if (stillFor.compareTo(BigDecimal.valueOf(MOVEMENT_LOG_MIN_INACTIVE_MINUTES)) >= 0) {
            registerDomainEvent(MovementDetectedEvent.from(this, sample.measuredAt(), stillFor));
        }
        if (status == ActivityStatus.INACTIVITY_DETECTED) {
            this.status = ActivityStatus.NORMAL;
            this.inactivitySince = null;
            registerDomainEvent(ActivityResumedEvent.from(this, sample.measuredAt()));
        }
        if (activeSince == null) {
            this.activeSince = sample.measuredAt();
            this.activeStreakSteps = 0;
        }
        this.activeStreakSteps = activeStreakSteps + sample.steps();
        this.lastMovementAt = sample.measuredAt();
        this.inactiveMinutes = BigDecimal.ZERO;
    }

    private void onStillness(ActivitySample sample, SleepWindow sleepWindow, ZoneId zone) {
        if (activeSince != null) {
            if (activeStreakSteps >= WALK_MIN_STEPS) {
                registerDomainEvent(WalkDetectedEvent.from(this, activeSince, sample.measuredAt(), activeStreakSteps));
            }
            this.activeSince = null;
            this.activeStreakSteps = 0;
        }
        this.inactiveMinutes = sample.inactiveMinutes();

        var duringWatchHours = !sleepWindow.contains(sample.measuredAt(), zone);
        var thresholdReached = inactiveMinutes.compareTo(BigDecimal.valueOf(detectionSettings.thresholdMinutes())) >= 0;
        if (detectionSettings.enabled() && duringWatchHours && thresholdReached
                && status != ActivityStatus.INACTIVITY_DETECTED) {
            this.status = ActivityStatus.INACTIVITY_DETECTED;
            this.inactivitySince = sample.measuredAt();
            registerDomainEvent(ProlongedInactivityDetectedEvent.from(this));
        }
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

    public void setDetectionSettings(InactivityDetectionSettings detectionSettings) {
        this.detectionSettings = detectionSettings;
    }

    public void setInactiveMinutes(BigDecimal inactiveMinutes) {
        this.inactiveMinutes = inactiveMinutes;
    }

    public void setLastMovementAt(Instant lastMovementAt) {
        this.lastMovementAt = lastMovementAt;
    }

    public void setLastSampleAt(Instant lastSampleAt) {
        this.lastSampleAt = lastSampleAt;
    }

    public void setActiveSince(Instant activeSince) {
        this.activeSince = activeSince;
    }

    public void setActiveStreakSteps(Integer activeStreakSteps) {
        this.activeStreakSteps = activeStreakSteps;
    }
}
