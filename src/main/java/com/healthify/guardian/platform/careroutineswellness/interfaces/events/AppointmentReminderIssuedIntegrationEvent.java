package com.healthify.guardian.platform.careroutineswellness.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event published by {@code careRoutinesWellness} when the reminder of a medical appointment is
 * issued, i.e. when the time left until the appointment matches the lead time the caregiver configured.
 *
 * <p>Consumed by {@code Emergency & Alerting} to send a preventive push notification to the caregivers of the
 * person under care.</p>
 *
 * @param reminderId        the issued appointment reminder
 * @param personUnderCareId the person the appointment belongs to
 * @param title             what the appointment is about, e.g. "Control de cardiología"
 * @param location          where it takes place, if known
 * @param scheduledTime     when the appointment takes place
 * @param issuedAt          when the reminder was issued
 */
public record AppointmentReminderIssuedIntegrationEvent(
        UUID reminderId,
        UUID personUnderCareId,
        String title,
        String location,
        Instant scheduledTime,
        Instant issuedAt) {
}
