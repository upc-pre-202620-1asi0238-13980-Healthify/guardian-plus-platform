package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Raised when {@code ReminderIssuancePolicy} decides a reminder must be issued.
 */
public record ReminderIssuedEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        ReminderType type,
        Instant issuedAt) {

    public static ReminderIssuedEvent from(Reminder reminder) {
        return new ReminderIssuedEvent(
                reminder.getId(), reminder.getPersonUnderCareId(), reminder.getType(), reminder.getIssuedAt());
    }
}
