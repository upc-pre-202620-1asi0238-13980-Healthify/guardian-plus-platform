package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing a reminder.
 *
 * @param id                 the reminder's unique identifier
 * @param personUnderCareId  the person the reminder belongs to
 * @param type               the kind of routine this reminder is about
 * @param scheduledTime      when the reminder must be issued
 * @param issuedAt           when the reminder was last issued or reissued, if it has been
 * @param status             the current lifecycle status
 * @param reissueCount       how many times this reminder has been reissued
 */
public record ReminderResource(
        UUID id,
        UUID personUnderCareId,
        String type,
        Instant scheduledTime,
        Instant issuedAt,
        String status,
        Integer reissueCount
) {
}
