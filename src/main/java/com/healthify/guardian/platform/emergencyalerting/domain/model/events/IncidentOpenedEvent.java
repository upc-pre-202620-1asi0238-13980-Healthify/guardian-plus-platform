package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;

import java.time.Instant;

/**
 * Raised when the human attention of an acknowledged alert starts being tracked.
 */
public record IncidentOpenedEvent(
        IncidentId incidentId,
        AlertId alertId,
        Instant markedInAttentionAt) {

    public static IncidentOpenedEvent from(Incident incident) {
        return new IncidentOpenedEvent(
                incident.getId(), incident.getAlertId(), incident.getMarkedInAttentionAt());
    }
}
