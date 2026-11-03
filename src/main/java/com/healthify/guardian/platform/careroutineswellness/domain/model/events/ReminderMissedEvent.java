package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Raised when the family member reviews an unconfirmed reminder and closes it as missed.
 */
public record ReminderMissedEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        ReminderType type,
        Instant missedAt) {

    public static ReminderMissedEvent from(Reminder reminder, Instant missedAt) {
        return new ReminderMissedEvent(reminder.getId(), reminder.getPersonUnderCareId(), reminder.getType(), missedAt);
    }
}
