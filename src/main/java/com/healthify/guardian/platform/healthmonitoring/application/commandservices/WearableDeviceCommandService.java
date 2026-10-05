package com.healthify.guardian.platform.healthmonitoring.application.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.AssignWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DeactivateWearableDeviceCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code WearableDevice} aggregate.
 */
public interface WearableDeviceCommandService {

    Result<WearableDevice, ApplicationError> handle(AssignWearableDeviceCommand command);

    Result<WearableDevice, ApplicationError> handle(DeactivateWearableDeviceCommand command);
}
