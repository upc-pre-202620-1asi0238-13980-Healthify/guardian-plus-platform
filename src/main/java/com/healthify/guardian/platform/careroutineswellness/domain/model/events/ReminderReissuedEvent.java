package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Raised when the Reminder Reissue Policy reissues a medication reminder
 * because it was not confirmed within the tolerance window.
 *
 * <p>Republished by {@code ReminderReissuedEventHandler} as a
 * {@code ReminderReissuedIntegrationEvent} for {@code Emergency & Alerting}.</p>
 */
public record ReminderReissuedEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        ReminderType type,
        Integer reissueCount,
        Instant reissuedAt) {

    public static ReminderReissuedEvent from(Reminder reminder) {
        return new ReminderReissuedEvent(
                reminder.getId(), reminder.getPersonUnderCareId(), reminder.getType(),
                reminder.getReissueCount(), reminder.getIssuedAt());
    }
}
