package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Raised when a due reminder is issued to the person under care.
 *
 * @param reminderId        the issued reminder
 * @param personUnderCareId the person the reminder belongs to
 * @param type              the reminder type
 * @param title             what the reminder is about, e.g. "Control de cardiología"
 * @param location          where it takes place; only for medical appointments, {@code null} otherwise
 * @param scheduledTime     when the routine itself takes place (not when the reminder was issued)
 * @param issuedAt          when the reminder was issued
 */
public record ReminderIssuedEvent(
        ReminderId reminderId,
        PersonUnderCareId personUnderCareId,
        ReminderType type,
        String title,
        String location,
        Instant scheduledTime,
        Instant issuedAt) {

    public static ReminderIssuedEvent from(Reminder reminder) {
        var details = reminder.getDetails();
        return new ReminderIssuedEvent(
                reminder.getId(),
                reminder.getPersonUnderCareId(),
                reminder.getType(),
                details == null ? null : details.title(),
                details == null ? null : details.location(),
                reminder.getScheduledTime(),
                reminder.getIssuedAt());
    }
}
