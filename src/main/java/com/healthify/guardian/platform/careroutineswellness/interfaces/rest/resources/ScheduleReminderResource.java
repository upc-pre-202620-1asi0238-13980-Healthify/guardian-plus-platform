package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * Request payload for scheduling a new reminder.
 *
 * @param personUnderCareId the person the reminder is scheduled for
 * @param type               the kind of routine this reminder is about
 * @param scheduledTime      when the reminder must be issued
 */
public record ScheduleReminderResource(

        @NotNull(message = "{reminder.person-under-care-id.blank}")
        UUID personUnderCareId,

        @NotNull(message = "{reminder.type.blank}")
        ReminderType type,

        @NotNull(message = "{reminder.scheduled-time.blank}")
        Instant scheduledTime
) {
}
