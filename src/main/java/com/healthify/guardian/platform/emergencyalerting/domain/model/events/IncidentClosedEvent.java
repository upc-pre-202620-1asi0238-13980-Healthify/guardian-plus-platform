package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;

import java.time.Instant;

/**
 * Raised when an incident is definitively closed.
 *
 * <p>Handled to resolve the alert and publish {@code IncidentClosedIntegrationEvent}.</p>
 */
public record IncidentClosedEvent(
        IncidentId incidentId,
        AlertId alertId,
        String notes,
        Instant closedAt) {

    public static IncidentClosedEvent from(Incident incident) {
        return new IncidentClosedEvent(
                incident.getId(), incident.getAlertId(), incident.getNotes(), incident.getClosedAt());
    }
}
