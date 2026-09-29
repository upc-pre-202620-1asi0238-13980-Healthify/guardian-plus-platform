package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.ReminderPersistenceEntity;

/**
 * Static assembler between the {@link Reminder} domain aggregate and its persistence entity.
 */
public final class ReminderPersistenceAssembler {

    private ReminderPersistenceAssembler() {
    }

    public static Reminder toDomainFromPersistence(ReminderPersistenceEntity entity) {
        if (entity == null) return null;
        var reminder = new Reminder();
        reminder.setId(new ReminderId(entity.getId()));
        reminder.setPersonUnderCareId(entity.getPersonUnderCareId());
        reminder.setType(entity.getType());
        reminder.setScheduledTime(entity.getScheduledTime());
        reminder.setIssuedAt(entity.getIssuedAt());
        reminder.setStatus(entity.getStatus());
        reminder.setReissueCount(entity.getReissueCount());
        return reminder;
    }

    public static ReminderPersistenceEntity toPersistenceFromDomain(Reminder reminder) {
        if (reminder == null) return null;
        var entity = new ReminderPersistenceEntity();
        entity.setId(reminder.getId().value());
        entity.setPersonUnderCareId(reminder.getPersonUnderCareId());
        entity.setType(reminder.getType());
        entity.setScheduledTime(reminder.getScheduledTime());
        entity.setIssuedAt(reminder.getIssuedAt());
        entity.setStatus(reminder.getStatus());
        entity.setReissueCount(reminder.getReissueCount());
        return entity;
    }
}
