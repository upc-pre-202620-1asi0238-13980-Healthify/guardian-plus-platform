package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Raised when a new reminder is scheduled.
 */
public record ReminderScheduledEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        ReminderType type,
        Instant scheduledTime) {

    public static ReminderScheduledEvent from(Reminder reminder) {
        return new ReminderScheduledEvent(
                reminder.getId(), reminder.getPersonUnderCareId(), reminder.getType(), reminder.getScheduledTime());
    }
}
