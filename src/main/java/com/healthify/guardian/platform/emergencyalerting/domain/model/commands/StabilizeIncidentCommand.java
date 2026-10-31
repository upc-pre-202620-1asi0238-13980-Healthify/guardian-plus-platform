package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to declare that the Fragile Citizen's situation has been stabilized.
 *
 * @param incidentId the incident to stabilize
 * @param notes      optional free-text notes
 */
public record StabilizeIncidentCommand(UUID incidentId, String notes) {
}
