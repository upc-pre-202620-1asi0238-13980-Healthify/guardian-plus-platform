package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to definitively close an incident.
 *
 * @param incidentId the incident to close
 * @param notes      optional free-text notes
 */
public record CloseIncidentCommand(UUID incidentId, String notes) {
}
