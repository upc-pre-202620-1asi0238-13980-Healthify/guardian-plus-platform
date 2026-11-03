package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to definitively close an alert once its incident has been closed.
 *
 * @param alertId the alert to resolve
 */
public record ResolveAlertCommand(UUID alertId) {
}
