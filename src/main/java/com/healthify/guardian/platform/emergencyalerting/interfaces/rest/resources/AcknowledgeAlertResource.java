package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload to acknowledge an alert.
 *
 * @param userId the notified Care Circle member acknowledging the alert
 */
public record AcknowledgeAlertResource(
        @NotNull(message = "{alert.user-id.blank}")
        UUID userId
) {
}
