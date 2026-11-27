package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to dismiss an alert as a false positive within its fall confirmation window.
 *
 * @param alertId the alert to dismiss
 */
public record DismissAlertCommand(UUID alertId) {
}
