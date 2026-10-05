package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.AssignWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.AssignWearableDeviceResource;

/**
 * Assembler converting an {@link AssignWearableDeviceResource} into an {@link AssignWearableDeviceCommand}.
 */
public final class AssignWearableDeviceCommandFromResourceAssembler {

    private AssignWearableDeviceCommandFromResourceAssembler() {
    }

    public static AssignWearableDeviceCommand toCommandFromResource(AssignWearableDeviceResource resource) {
        return new AssignWearableDeviceCommand(resource.careRecipientProfileId(), resource.serialNumber(), resource.deviceType());
    }
}
