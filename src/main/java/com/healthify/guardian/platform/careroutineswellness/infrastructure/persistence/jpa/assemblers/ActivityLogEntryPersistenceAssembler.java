package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.ActivityLogEntry;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ActivityLogEntryId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ActivityLogEntryPersistenceEntity;

/**
 * Static assembler between the {@link ActivityLogEntry} domain aggregate and its persistence entity.
 */
public final class ActivityLogEntryPersistenceAssembler {

    private ActivityLogEntryPersistenceAssembler() {
    }

    public static ActivityLogEntry toDomainFromPersistence(ActivityLogEntryPersistenceEntity entity) {
        if (entity == null) return null;
        var entry = new ActivityLogEntry();
        entry.setId(new ActivityLogEntryId(entity.getId()));
        entry.setPersonUnderCareId(entity.getPersonUnderCareId());
        entry.setType(entity.getType());
        entry.setOccurredAt(entity.getOccurredAt());
        entry.setDurationMinutes(entity.getDurationMinutes());
        entry.setSteps(entity.getSteps());
        entry.setInactiveMinutes(entity.getInactiveMinutes());
        return entry;
    }

    public static ActivityLogEntryPersistenceEntity toPersistenceFromDomain(ActivityLogEntry entry) {
        if (entry == null) return null;
        var entity = new ActivityLogEntryPersistenceEntity();
        entity.setId(entry.getId().value());
        entity.setPersonUnderCareId(entry.getPersonUnderCareId());
        entity.setType(entry.getType());
        entity.setOccurredAt(entry.getOccurredAt());
        entity.setDurationMinutes(entry.getDurationMinutes());
        entity.setSteps(entry.getSteps());
        entity.setInactiveMinutes(entry.getInactiveMinutes());
        return entity;
    }
}
