package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to record that a recipient will take charge of responding to an alert.
 *
 * @param alertId         the alert being responded to
 * @param responderUserId the recipient taking charge
 */
public record ClaimAlertResponseCommand(UUID alertId, UUID responderUserId) {
}
