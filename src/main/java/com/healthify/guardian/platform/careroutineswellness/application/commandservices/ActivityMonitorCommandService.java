package com.healthify.guardian.platform.careroutineswellness.application.commandservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfigureInactivityDetectionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordActivitySampleCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code ActivityMonitor} aggregate.
 */
public interface ActivityMonitorCommandService {

    /**
     * Applies a periodic kinematic sample reported by the wearable device.
     *
     * @param command the activity telemetry
     * @return the updated activity monitor or an application error
     */
    Result<ActivityMonitor, ApplicationError> handle(RecordActivitySampleCommand command);

    /**
     * Changes how prolonged inactivity is detected for a person under care.
     *
     * @param command the new detection settings
     * @return the updated activity monitor or an application error
     */
    Result<ActivityMonitor, ApplicationError> handle(ConfigureInactivityDetectionCommand command);
}
