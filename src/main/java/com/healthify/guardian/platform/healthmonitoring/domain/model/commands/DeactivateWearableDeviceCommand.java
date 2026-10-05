package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import java.util.UUID;

/**
 * Command to deactivate an assigned wearable device.
 */
public record DeactivateWearableDeviceCommand(UUID wearableDeviceId) {
}
