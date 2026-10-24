package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;

import java.time.Instant;

/**
 * Raised when the responder declares the Fragile Citizen's situation stabilized.
 */
public record IncidentStabilizedEvent(
        IncidentId incidentId,
        AlertId alertId,
        Instant stabilizedAt) {

    public static IncidentStabilizedEvent from(Incident incident) {
        return new IncidentStabilizedEvent(
                incident.getId(), incident.getAlertId(), incident.getStabilizedAt());
    }
}
