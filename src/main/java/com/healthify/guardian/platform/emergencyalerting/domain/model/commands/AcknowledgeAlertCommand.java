package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to record that a recipient acknowledged an alert, stopping its escalation.
 *
 * @param alertId the alert being acknowledged
 * @param userId  the recipient acknowledging it
 */
public record AcknowledgeAlertCommand(UUID alertId, UUID userId) {
}
