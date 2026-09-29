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
        return new ScheduleReminderCommand(resource.personUnderCareId(), resource.type(), resource.scheduledTime());
    }
}
