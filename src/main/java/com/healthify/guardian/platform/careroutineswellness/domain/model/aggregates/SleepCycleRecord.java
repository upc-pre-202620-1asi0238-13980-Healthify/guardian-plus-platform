package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordSleepCycleCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.SleepCycleRecordedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepClassification;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepCycleRecordId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;

/**
 * Aggregate root representing a single closed sleep cycle for a person under care.
 */
@Getter
public class SleepCycleRecord extends AbstractDomainAggregateRoot<SleepCycleRecord> {

    /**
     * Business rule from the report: a night with more interruptions than this is classified as
     * fragmented rather than regular.
     */
    private static final int FRAGMENTED_INTERRUPTION_THRESHOLD = 4;
    /** Interruptions at which continuity drops to zero. */
    private static final double MAX_SCORED_INTERRUPTIONS = 8.0;
    /** Hours of sleep considered a complete night when scoring continuity. */
    private static final double FULL_NIGHT_HOURS = 8.0;

    private static final String TIMES_INVALID_MESSAGE_KEY = "sleep-cycle-record.times.invalid";
    private static final String INTERRUPTIONS_INVALID_MESSAGE_KEY = "sleep-cycle-record.interruption-count.invalid";

    private SleepCycleRecordId id;
    private PersonUnderCareId personUnderCareId;
    private Instant startTime;
    private Instant endTime;
    private Integer interruptionCount;
    private SleepClassification classification;

    /** Reconstitution constructor, used by the persistence assembler. */
    public SleepCycleRecord() {
    }

    /** Creates and classifies a new closed sleep cycle from a recording command. */
    public SleepCycleRecord(RecordSleepCycleCommand command) {
        if (command.startTime() == null || command.endTime() == null || !command.endTime().isAfter(command.startTime())) {
            throw new IllegalArgumentException(TIMES_INVALID_MESSAGE_KEY);
        }
        if (command.interruptionCount() == null || command.interruptionCount() < 0) {
            throw new IllegalArgumentException(INTERRUPTIONS_INVALID_MESSAGE_KEY);
        }
        this.id = SleepCycleRecordId.generate();
        this.personUnderCareId = new PersonUnderCareId(command.personUnderCareId());
        this.startTime = command.startTime();
        this.endTime = command.endTime();
        this.interruptionCount = command.interruptionCount();
        this.classification = classify();
        registerDomainEvent(SleepCycleRecordedEvent.from(this));
    }

    /**
     * Classifies this sleep cycle based on how many interruptions were detected during it.
     *
     * @return {@code FRAGMENTED} when the interruption count exceeds the threshold, {@code REGULAR} otherwise
     */
    public SleepClassification classify() {
        return interruptionCount != null && interruptionCount > FRAGMENTED_INTERRUPTION_THRESHOLD
                ? SleepClassification.FRAGMENTED
                : SleepClassification.REGULAR;
    }

    /** Total time asleep, in whole minutes. */
    public long durationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Scores how continuous the night was, from 0 to 100: every interruption lowers the score, and so does
     * a night shorter than a full one.
     *
     * @return the continuity score, as a percentage
     */
    public int continuityScore() {
        var interruptionFactor = Math.max(0.0, 1.0 - interruptionCount / MAX_SCORED_INTERRUPTIONS);
        var lengthFactor = Math.min(durationMinutes() / 60.0 / FULL_NIGHT_HOURS, 1.0);
        return (int) Math.round(interruptionFactor * lengthFactor * 100);
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(SleepCycleRecordId id) {
        this.id = id;
    }

    public void setPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        this.personUnderCareId = personUnderCareId;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public void setInterruptionCount(Integer interruptionCount) {
        this.interruptionCount = interruptionCount;
    }

    public void setClassification(SleepClassification classification) {
        this.classification = classification;
    }
}
