package com.healthify.guardian.platform.careroutineswellness.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published by {@code careRoutinesWellness} when a medication reminder is
 * reissued after not being confirmed within the tolerance window.
 *
 * <p>Consumed by {@code Emergency & Alerting} to raise a {@code REMINDER_REISSUED} alert of
 * {@code MEDIUM} severity directed at the primary contact.</p>
 *
 * @param reminderId        the reissued reminder
 * @param personUnderCareId the person the reminder belongs to
 * @param reminderType       the reminder type, always {@code MEDICATION} for this event
 * @param reissueCount       how many times this reminder has been reissued so far
 * @param reissuedAt         when the reissue happened
 */
public record ReminderReissuedIntegrationEvent(
        UUID reminderId,
        UUID personUnderCareId,
        String reminderType,
        Integer reissueCount,
        Instant reissuedAt) {
}
