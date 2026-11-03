package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Request payload for scheduling a new reminder.
 *
 * @param personUnderCareId the person the reminder is scheduled for
 * @param type              the kind of routine this reminder is about
 * @param scheduledTime     when the routine happens: the dose, the appointment or the activity
 * @param title             medication name, appointment specialty or activity name
 * @param dosage            dose to take, for medication reminders (e.g. "50 mg")
 * @param instructions      free-text indications (e.g. "Después del almuerzo")
 * @param location          medical center, for appointments
 * @param durationMinutes   activity length, for physical activity reminders
 * @param leadTimeMinutes   how long before {@code scheduledTime} the reminder is issued (e.g. 60 for
 *                          "1 hora antes"); zero when omitted
 * @param medicationStockId the stock a confirmed dose is discounted from; when omitted, the stock of the
 *                          medication named like {@code title} is used, if any
 * @param recurrence        how the reminder repeats; once when omitted
 */
public record ScheduleReminderResource(

        @NotNull(message = "{reminder.person-under-care-id.blank}")
        UUID personUnderCareId,

        @NotNull(message = "{reminder.type.blank}")
        ReminderType type,

        @NotNull(message = "{reminder.scheduled-time.blank}")
        Instant scheduledTime,

        @NotBlank(message = "{reminder.title.blank}")
        @Size(max = 255, message = "{reminder.text.too-long}")
        String title,

        @Size(max = 255, message = "{reminder.text.too-long}")
        String dosage,

        @Size(max = 500, message = "{reminder.text.too-long}")
        String instructions,

        @Size(max = 255, message = "{reminder.text.too-long}")
        String location,

        @Positive(message = "{reminder.duration-minutes.invalid}")
        Integer durationMinutes,

        @PositiveOrZero(message = "{reminder.lead-time-minutes.invalid}")
        Integer leadTimeMinutes,

        UUID medicationStockId,

        RecurrenceResource recurrence
) {
}
