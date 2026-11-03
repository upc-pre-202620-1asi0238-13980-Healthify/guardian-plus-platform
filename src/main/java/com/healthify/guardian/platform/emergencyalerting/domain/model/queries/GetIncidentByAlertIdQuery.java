package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;

/**
 * Query to retrieve the incident opened for an alert.
 *
 * @param alertId the alert whose incident is requested
 */
public record GetIncidentByAlertIdQuery(AlertId alertId) {
}
