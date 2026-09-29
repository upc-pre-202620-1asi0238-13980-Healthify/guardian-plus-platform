package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Raised when {@code ReminderIssuancePolicy} suppresses a hydration reminder that fell
 * inside the configured sleep window instead of issuing it.
 */
public record ReminderSuppressedEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        ReminderType type,
        Instant suppressedAt) {

    public static ReminderSuppressedEvent from(Reminder reminder, Instant suppressedAt) {
        return new ReminderSuppressedEvent(
                reminder.getId(), reminder.getPersonUnderCareId(), reminder.getType(), suppressedAt);
    }
}
