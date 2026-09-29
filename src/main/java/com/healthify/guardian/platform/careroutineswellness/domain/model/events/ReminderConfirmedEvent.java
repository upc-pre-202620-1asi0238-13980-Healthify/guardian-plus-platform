package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Raised when the person under care confirms an issued (or reissued) reminder.
 *
 * <p>Consumed internally by {@code ReminderConfirmedEventHandler}, which registers the
 * corresponding dose consumption in {@code MedicationStock} when {@code type == MEDICATION}.</p>
 */
public record ReminderConfirmedEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        ReminderType type,
        Instant confirmedAt) {

    public static ReminderConfirmedEvent from(Reminder reminder, Instant confirmedAt) {
        return new ReminderConfirmedEvent(
                reminder.getId(), reminder.getPersonUnderCareId(), reminder.getType(), confirmedAt);
    }
}
