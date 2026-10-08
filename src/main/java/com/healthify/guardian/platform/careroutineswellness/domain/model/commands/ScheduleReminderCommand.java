package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RecurrenceFrequency;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Command to schedule a new reminder (the first occurrence of a possibly recurring series) for a
 * person under care.
 *
 * @param personUnderCareId   the person the reminder is scheduled for
 * @param type                the kind of routine this reminder is about
 * @param scheduledTime       when the routine itself happens (the dose, the appointment, the activity)
 * @param title               short name of the routine, e.g. the medication or the appointment specialty
 * @param dosage              dose to take, for medication reminders
 * @param instructions        free-text indications
 * @param location            where an appointment takes place
 * @param durationMinutes     how long a physical activity lasts
 * @param leadTimeMinutes     how long before {@code scheduledTime} the reminder is issued; zero when null
 * @param medicationStockId   the medication stock a confirmed dose is discounted from, if any
 * @param recurrenceFrequency how often the reminder repeats; {@code ONCE} when null
 * @param daysOfWeek          days a {@code WEEKLY} reminder repeats on
 * @param intervalHours       hours between occurrences of an {@code HOURLY} reminder
 */
public record ScheduleReminderCommand(
        UUID personUnderCareId,
        ReminderType type,
        Instant scheduledTime,
        String title,
        String dosage,
        String instructions,
        String location,
        Integer durationMinutes,
        Integer leadTimeMinutes,
        UUID medicationStockId,
        RecurrenceFrequency recurrenceFrequency,
        Set<DayOfWeek> daysOfWeek,
        Integer intervalHours) {
}
