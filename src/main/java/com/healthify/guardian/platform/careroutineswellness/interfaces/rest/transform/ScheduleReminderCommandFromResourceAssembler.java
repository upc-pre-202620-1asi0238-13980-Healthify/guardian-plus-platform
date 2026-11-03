package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ScheduleReminderResource;

/**
 * Assembler to convert a {@link ScheduleReminderResource} to a {@link ScheduleReminderCommand}.
 */
public final class ScheduleReminderCommandFromResourceAssembler {

    private ScheduleReminderCommandFromResourceAssembler() {
    }

    public static ScheduleReminderCommand toCommandFromResource(ScheduleReminderResource resource) {
        var recurrence = resource.recurrence();
        return new ScheduleReminderCommand(
                resource.personUnderCareId(),
                resource.type(),
                resource.scheduledTime(),
                resource.title(),
                resource.dosage(),
                resource.instructions(),
                resource.location(),
                resource.durationMinutes(),
                resource.leadTimeMinutes(),
                resource.medicationStockId(),
                recurrence == null ? null : recurrence.frequency(),
                recurrence == null ? null : recurrence.daysOfWeek(),
                recurrence == null ? null : recurrence.intervalHours());
    }
}
