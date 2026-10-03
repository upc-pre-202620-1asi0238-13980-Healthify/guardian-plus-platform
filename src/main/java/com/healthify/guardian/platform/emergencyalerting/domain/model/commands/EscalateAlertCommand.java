package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to escalate an unacknowledged alert to its secondary emergency contacts.
 *
 * @param alertId the alert to escalate
 */
public record EscalateAlertCommand(UUID alertId) {
}
