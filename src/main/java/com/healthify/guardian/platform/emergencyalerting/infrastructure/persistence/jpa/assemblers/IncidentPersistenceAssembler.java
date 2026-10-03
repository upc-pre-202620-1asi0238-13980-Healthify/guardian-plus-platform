package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.IncidentId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.IncidentPersistenceEntity;

/**
 * Static assembler between the {@link Incident} domain aggregate and its persistence entity.
 */
public final class IncidentPersistenceAssembler {

    private IncidentPersistenceAssembler() {
    }

    public static Incident toDomainFromPersistence(IncidentPersistenceEntity entity) {
        if (entity == null) return null;
        var incident = new Incident();
        incident.setId(new IncidentId(entity.getId()));
        incident.setAlertId(new AlertId(entity.getAlertId()));
        incident.setStatus(entity.getStatus());
        incident.setMarkedInAttentionAt(entity.getMarkedInAttentionAt());
        incident.setStabilizedAt(entity.getStabilizedAt());
        incident.setClosedAt(entity.getClosedAt());
        incident.setNotes(entity.getNotes());
        return incident;
    }

    public static IncidentPersistenceEntity toPersistenceFromDomain(Incident incident) {
        if (incident == null) return null;
        var entity = new IncidentPersistenceEntity();
        entity.setId(incident.getId().value());
        entity.setAlertId(incident.getAlertId().value());
        entity.setStatus(incident.getStatus());
        entity.setMarkedInAttentionAt(incident.getMarkedInAttentionAt());
        entity.setStabilizedAt(incident.getStabilizedAt());
        entity.setClosedAt(incident.getClosedAt());
        entity.setNotes(incident.getNotes());
        return entity;
    }
}
