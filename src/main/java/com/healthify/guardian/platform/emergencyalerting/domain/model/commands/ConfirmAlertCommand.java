package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to confirm an alert, either after its fall confirmation window expires or right away
 * when its source does not require one.
 *
 * @param alertId the alert to confirm
 */
public record ConfirmAlertCommand(UUID alertId) {
}
