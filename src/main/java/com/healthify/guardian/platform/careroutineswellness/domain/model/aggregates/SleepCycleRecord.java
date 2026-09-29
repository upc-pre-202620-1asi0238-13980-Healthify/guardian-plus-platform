package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordSleepCycleCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.SleepCycleRecordedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepClassification;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepCycleRecordId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;

/**
 * Aggregate root representing a single closed sleep cycle for a person under care.
 */
@Getter
public class SleepCycleRecord extends AbstractDomainAggregateRoot<SleepCycleRecord> {

    /**
     * A sleep cycle with more interruptions than this is classified as fragmented rather
     * than regular.
     */
    private static final int FRAGMENTED_INTERRUPTION_THRESHOLD = 2;

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
