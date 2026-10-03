package com.healthify.guardian.platform.careroutineswellness.application.commandservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordActivityResumedCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordProlongedInactivityCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code ActivityMonitor} aggregate.
 */
public interface ActivityMonitorCommandService {

    /**
     * Records that prolonged physical inactivity was detected for a person under care.
     *
     * @param command the inactivity telemetry
     * @return the updated activity monitor or an application error
     */
    Result<ActivityMonitor, ApplicationError> handle(RecordProlongedInactivityCommand command);

    /**
     * Records that physical activity resumed for a person under care.
     *
     * @param command the activity-resumed telemetry
     * @return the updated activity monitor or an application error
     */
    Result<ActivityMonitor, ApplicationError> handle(RecordActivityResumedCommand command);
}
