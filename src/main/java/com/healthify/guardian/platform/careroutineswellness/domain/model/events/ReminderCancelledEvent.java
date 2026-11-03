package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;

import java.time.Instant;

/**
 * Raised when a reminder is cancelled.
 */
public record ReminderCancelledEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        Instant cancelledAt) {

    public static ReminderCancelledEvent from(Reminder reminder, Instant cancelledAt) {
        return new ReminderCancelledEvent(reminder.getId(), reminder.getPersonUnderCareId(), cancelledAt);
    }
}
