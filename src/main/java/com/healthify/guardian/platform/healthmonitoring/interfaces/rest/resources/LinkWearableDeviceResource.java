package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload linking a wearable device to a care recipient.
 */
public record LinkWearableDeviceResource(
        @NotNull(message = "{care-recipient-profile.id.invalid}")
        UUID careRecipientProfileId,

        @NotBlank(message = "{wearable-device.serial-number.invalid}")
        @Schema(example = "GP-ESP32-S3-0001")
        String serialNumber,

        @NotBlank(message = "{wearable-device.device-type.invalid}")
        @Schema(allowableValues = {"SMARTWATCH", "WRISTBAND", "PATCH"}, example = "WRISTBAND")
        String deviceType
) {
}
