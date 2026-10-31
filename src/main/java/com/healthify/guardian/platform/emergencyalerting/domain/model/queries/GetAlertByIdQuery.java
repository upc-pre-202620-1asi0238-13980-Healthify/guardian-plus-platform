package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;

/**
 * Query to retrieve a single alert with its deliveries and responses.
 *
 * @param alertId the alert to retrieve
 */
public record GetAlertByIdQuery(AlertId alertId) {
}
