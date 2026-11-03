package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.LinkWearableDeviceCommand;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LinkWearableDeviceResource;

/**
 * Assembler converting a {@link LinkWearableDeviceResource} into a {@link LinkWearableDeviceCommand}.
 */
public final class LinkWearableDeviceCommandFromResourceAssembler {

    private LinkWearableDeviceCommandFromResourceAssembler() {
    }

    public static LinkWearableDeviceCommand toCommandFromResource(LinkWearableDeviceResource resource) {
        return new LinkWearableDeviceCommand(resource.careRecipientProfileId(), resource.serialNumber(), resource.deviceType());
    }
}
