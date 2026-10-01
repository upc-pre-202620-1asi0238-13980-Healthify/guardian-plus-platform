package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload the wearable gateway sends when it detects a fall or the SOS button is pressed.
 *
 * @param careRecipientProfileId the Fragile Citizen wearing the device
 * @param sourceType             {@code FALL_DETECTED} or {@code SOS_TRIGGERED}
 * @param sourceReferenceId      the wearable device that raised the signal
 */
public record TriggerAlertResource(
        @NotNull(message = "{alert.care-recipient-profile-id.blank}")
        UUID careRecipientProfileId,

        @NotNull(message = "{alert.source-type.blank}")
        AlertSourceType sourceType,

        @NotNull(message = "{alert.source-reference-id.blank}")
        UUID sourceReferenceId
) {
}
