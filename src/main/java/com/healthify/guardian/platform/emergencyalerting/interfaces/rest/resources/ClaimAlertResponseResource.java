package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload to take charge of responding to an alert.
 *
 * @param responderUserId the notified Care Circle member taking charge
 */
public record ClaimAlertResponseResource(
        @NotNull(message = "{alert.user-id.blank}")
        UUID responderUserId
) {
}
