package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.RecurrenceResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ReminderResource;

/**
 * Assembler that converts a {@link Reminder} domain aggregate into a {@link ReminderResource}.
 */
public final class ReminderResourceFromEntityAssembler {

    private ReminderResourceFromEntityAssembler() {
    }

    public static ReminderResource toResourceFromEntity(Reminder reminder) {
        var details = reminder.getDetails();
        var recurrence = reminder.getRecurrence();
        return new ReminderResource(
                reminder.getId().value(),
                reminder.getSeriesId().value(),
                reminder.getPersonUnderCareId().value(),
                reminder.getType().name(),
                details.title(),
                details.dosage(),
                details.instructions(),
                details.location(),
                details.durationMinutes(),
                reminder.getScheduledTime(),
                reminder.getLeadTimeMinutes(),
                reminder.notifyAt(),
                new RecurrenceResource(recurrence.frequency(), recurrence.daysOfWeek(), recurrence.intervalHours()),
                reminder.getMedicationStockId() == null ? null : reminder.getMedicationStockId().value(),
                reminder.getIssuedAt(),
                reminder.getConfirmedAt(),
                reminder.getStatus().name(),
                reminder.getReissueCount());
    }
}
