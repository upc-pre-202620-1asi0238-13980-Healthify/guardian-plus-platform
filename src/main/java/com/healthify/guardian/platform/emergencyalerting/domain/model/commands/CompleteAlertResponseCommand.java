package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to record the outcome of a recipient's intervention.
 *
 * @param alertId    the alert the response belongs to
 * @param responseId the response being completed
 * @param notes      free-text outcome of the intervention
 */
public record CompleteAlertResponseCommand(UUID alertId, UUID responseId, String notes) {
}
