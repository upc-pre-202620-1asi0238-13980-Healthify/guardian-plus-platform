package com.healthify.guardian.platform.healthmonitoring.application.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.LinkWearableDeviceCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code WearableDevice} aggregate.
 */
public interface WearableDeviceCommandService {

    /**
     * Links a wearable device to a care recipient. A serial number can only be linked once.
     */
    Result<WearableDevice, ApplicationError> handle(LinkWearableDeviceCommand command);
}
