package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;

/**
 * Command to unlink a wearable device from its care recipient, so its readings are no longer accepted. The readings
 * it already sent stay in the history, and its serial number can be linked again.
 */
public record UnlinkWearableDeviceCommand(WearableDeviceId wearableDeviceId) {
}
