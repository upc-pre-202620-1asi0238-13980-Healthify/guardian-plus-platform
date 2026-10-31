package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.WearableDevice;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.WearableDeviceResource;

/**
 * Assembler converting a {@link WearableDevice} aggregate into a {@link WearableDeviceResource}.
 */
public final class WearableDeviceResourceFromEntityAssembler {

    private WearableDeviceResourceFromEntityAssembler() {
    }

    public static WearableDeviceResource toResourceFromEntity(WearableDevice device) {
        return new WearableDeviceResource(
                device.getId().value(),
                device.getCareRecipientProfileId().value(),
                device.getSerialNumber().value(),
                device.getDeviceType().name(),
                device.getLinkedAt());
    }
}
