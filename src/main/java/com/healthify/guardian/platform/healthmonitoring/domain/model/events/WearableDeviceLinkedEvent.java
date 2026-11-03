package com.healthify.guardian.platform.healthmonitoring.domain.model.events;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.SerialNumber;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;

import java.time.Instant;

/**
 * Raised when a wearable device is linked to a care recipient and starts being accepted as their telemetry source.
 */
public record WearableDeviceLinkedEvent(
        WearableDeviceId wearableDeviceId,
        CareRecipientProfileId careRecipientProfileId,
        SerialNumber serialNumber,
        Instant linkedAt) {
}
