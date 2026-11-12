package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.SleepCycleRecordResource;

/**
 * Assembler that converts a {@link SleepCycleRecord} domain aggregate into a {@link SleepCycleRecordResource}.
 */
public final class SleepCycleRecordResourceFromEntityAssembler {

    private SleepCycleRecordResourceFromEntityAssembler() {
    }

    public static SleepCycleRecordResource toResourceFromEntity(SleepCycleRecord record) {
        return new SleepCycleRecordResource(
                record.getId().value(),
                record.getPersonUnderCareId().value(),
                record.getStartTime(),
                record.getEndTime(),
                record.durationMinutes(),
                record.getInterruptionCount(),
                record.continuityScore(),
                record.getClassification().name());
    }
}
