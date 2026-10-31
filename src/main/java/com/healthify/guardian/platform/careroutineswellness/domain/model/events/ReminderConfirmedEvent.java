package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Raised when the person under care confirms an issued (or reissued) reminder.
 *
 * <p>Consumed internally by {@code ReminderConfirmedEventHandler}, which registers the
 * corresponding dose consumption in the matching {@code MedicationStock} when {@code type == MEDICATION}.</p>
 *
 * @param medicationStockId the stock explicitly linked to the reminder, or null
 * @param title             the reminder's title, used to find the stock by medication name when none is linked
 */
public record ReminderConfirmedEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        ReminderType type,
        MedicationStockId medicationStockId,
        String title,
        Instant confirmedAt) {

    public static ReminderConfirmedEvent from(Reminder reminder) {
        return new ReminderConfirmedEvent(
                reminder.getId(), reminder.getPersonUnderCareId(), reminder.getType(),
                reminder.getMedicationStockId(), reminder.getDetails().title(), reminder.getConfirmedAt());
    }
}
