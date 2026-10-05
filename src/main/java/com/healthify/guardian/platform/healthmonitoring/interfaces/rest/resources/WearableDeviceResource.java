package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload of a wearable device.
 */
public record WearableDeviceResource(
        UUID id,
        UUID careRecipientProfileId,
        String serialNumber,
        String deviceType,
        Instant linkedAt
) {
}
