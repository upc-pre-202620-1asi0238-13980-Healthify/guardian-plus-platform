package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ReminderResource;

/**
 * Assembler that converts a {@link Reminder} domain aggregate into a {@link ReminderResource}.
 */
public final class ReminderResourceFromEntityAssembler {

    private ReminderResourceFromEntityAssembler() {
    }

    public static ReminderResource toResourceFromEntity(Reminder reminder) {
        return new ReminderResource(
                reminder.getId().value(),
                reminder.getPersonUnderCareId().value(),
                reminder.getType().name(),
                reminder.getScheduledTime(),
                reminder.getIssuedAt(),
                reminder.getStatus().name(),
                reminder.getReissueCount());
    }
}
