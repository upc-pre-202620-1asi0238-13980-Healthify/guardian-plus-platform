package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityMonitor;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityMonitorId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ActivityMonitorPersistenceEntity;

/**
 * Static assembler between the {@link ActivityMonitor} domain aggregate and its persistence entity.
 */
public final class ActivityMonitorPersistenceAssembler {

    private ActivityMonitorPersistenceAssembler() {
    }

    public static ActivityMonitor toDomainFromPersistence(ActivityMonitorPersistenceEntity entity) {
        if (entity == null) return null;
        var monitor = new ActivityMonitor();
        monitor.setId(new ActivityMonitorId(entity.getId()));
        monitor.setPersonUnderCareId(entity.getPersonUnderCareId());
        monitor.setStatus(entity.getStatus());
        monitor.setInactivitySince(entity.getInactivitySince());
        return monitor;
    }

    public static ActivityMonitorPersistenceEntity toPersistenceFromDomain(ActivityMonitor monitor) {
        if (monitor == null) return null;
        var entity = new ActivityMonitorPersistenceEntity();
        entity.setId(monitor.getId().value());
        entity.setPersonUnderCareId(monitor.getPersonUnderCareId());
        entity.setStatus(monitor.getStatus());
        entity.setInactivitySince(monitor.getInactivitySince());
        return entity;
    }
}
