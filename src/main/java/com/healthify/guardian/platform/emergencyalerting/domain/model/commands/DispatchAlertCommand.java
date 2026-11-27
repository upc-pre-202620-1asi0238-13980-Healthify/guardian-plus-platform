package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to generate and send the deliveries of a confirmed alert's initial recipient level.
 *
 * @param alertId the alert to dispatch
 */
public record DispatchAlertCommand(UUID alertId) {
}
