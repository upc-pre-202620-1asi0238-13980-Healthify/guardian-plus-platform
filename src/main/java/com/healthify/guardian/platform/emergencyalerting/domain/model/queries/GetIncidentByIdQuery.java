package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;

/**
 * Query to retrieve a single incident.
 *
 * @param incidentId the incident to retrieve
 */
public record GetIncidentByIdQuery(IncidentId incidentId) {
}
