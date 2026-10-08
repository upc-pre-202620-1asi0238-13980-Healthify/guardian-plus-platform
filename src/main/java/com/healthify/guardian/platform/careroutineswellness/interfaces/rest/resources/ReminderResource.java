package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing one occurrence of a reminder.
 *
 * @param id                the reminder's unique identifier
 * @param seriesId          identifier shared by every occurrence of the same recurring reminder
 * @param personUnderCareId the person the reminder belongs to
 * @param type              the kind of routine this reminder is about
 * @param title             medication name, appointment specialty or activity name
 * @param dosage            dose to take, for medication reminders
 * @param instructions      free-text indications
 * @param location          medical center, for appointments
 * @param durationMinutes   activity length, for physical activity reminders
 * @param scheduledTime     when the routine happens
 * @param leadTimeMinutes   how long before {@code scheduledTime} the reminder is issued
 * @param notifyAt          when the reminder reaches the person ({@code scheduledTime - leadTimeMinutes})
 * @param recurrence        how the reminder repeats
 * @param medicationStockId the stock explicitly linked to this medication reminder, if any
 * @param issuedAt          when the reminder was last issued or reissued, if it has been
 * @param confirmedAt       when the person confirmed it, if they did
 * @param status            the current lifecycle status
 * @param reissueCount      how many times this reminder has been reissued
 */
public record ReminderResource(
        UUID id,
        UUID seriesId,
        UUID personUnderCareId,
        String type,
        String title,
        String dosage,
        String instructions,
        String location,
        Integer durationMinutes,
        Instant scheduledTime,
        Integer leadTimeMinutes,
        Instant notifyAt,
        RecurrenceResource recurrence,
        UUID medicationStockId,
        Instant issuedAt,
        Instant confirmedAt,
        String status,
        Integer reissueCount
) {
}
