package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.util.UUID;

/**
 * Command to link a wearable device to a care recipient, so it is accepted as the source of their vital signs.
 */
public record LinkWearableDeviceCommand(UUID careRecipientProfileId, String serialNumber, String deviceType) {
}
