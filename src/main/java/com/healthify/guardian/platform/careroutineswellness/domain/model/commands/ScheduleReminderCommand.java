package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to schedule a new reminder for a person under care.
 *
 * @param personUnderCareId the person the reminder is scheduled for
 * @param type               the kind of routine this reminder is about
 * @param scheduledTime      when the reminder must be issued
 */
public record ScheduleReminderCommand(UUID personUnderCareId, ReminderType type, Instant scheduledTime) {
}
