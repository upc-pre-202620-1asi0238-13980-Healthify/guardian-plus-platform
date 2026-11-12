package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to broadcast an unacknowledged alert to every active emergency contact.
 *
 * @param alertId the alert to broadcast
 */
public record BroadcastAlertCommand(UUID alertId) {
}
