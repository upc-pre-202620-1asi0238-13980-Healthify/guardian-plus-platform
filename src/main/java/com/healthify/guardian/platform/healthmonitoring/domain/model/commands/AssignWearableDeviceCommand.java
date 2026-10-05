package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.util.UUID;

/**
 * Command to assign a wearable device to a care recipient.
 */
public record AssignWearableDeviceCommand(UUID careRecipientProfileId, String serialNumber, String deviceType) {
}
