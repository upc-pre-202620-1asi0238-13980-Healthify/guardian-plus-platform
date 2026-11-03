package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.IncidentResource;

/**
 * Assembler that converts an {@link Incident} domain aggregate into an {@link IncidentResource}.
 */
public final class IncidentResourceFromEntityAssembler {

    private IncidentResourceFromEntityAssembler() {
    }

    public static IncidentResource toResourceFromEntity(Incident incident) {
        return new IncidentResource(
                incident.getId().value(),
                incident.getAlertId().value(),
                incident.getStatus().name(),
                incident.getMarkedInAttentionAt(),
                incident.getStabilizedAt(),
                incident.getClosedAt(),
                incident.getNotes());
    }
}
