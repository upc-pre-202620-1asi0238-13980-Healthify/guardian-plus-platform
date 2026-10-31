package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepClassification;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepCycleRecordId;

import java.time.Instant;

/**
 * Raised after a sleep cycle has been recorded and classified.
 */
public record SleepCycleRecordedEvent(
        SleepCycleRecordId sleepCycleRecordId,
        PersonUnderCareId personUnderCareId,
        SleepClassification classification,
        Instant startTime,
        Instant endTime) {

    public static SleepCycleRecordedEvent from(SleepCycleRecord record) {
        return new SleepCycleRecordedEvent(
                record.getId(), record.getPersonUnderCareId(), record.getClassification(),
                record.getStartTime(), record.getEndTime());
    }
}
