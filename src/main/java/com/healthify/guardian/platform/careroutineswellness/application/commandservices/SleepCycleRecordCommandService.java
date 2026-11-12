package com.healthify.guardian.platform.careroutineswellness.application.commandservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordSleepCycleCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code SleepCycleRecord} aggregate.
 */
public interface SleepCycleRecordCommandService {

    /**
     * Records and classifies a newly closed sleep cycle.
     *
     * @param command the sleep cycle data reported by the wearable device
     * @return the recorded sleep cycle or an application error
     */
    Result<SleepCycleRecord, ApplicationError> handle(RecordSleepCycleCommand command);
}
