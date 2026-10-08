package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to schedule the occurrence that follows the given reminder in its recurring series.
 * Has no effect on one-off reminders.
 *
 * @param reminderId the occurrence that has just left the {@code SCHEDULED} status
 */
public record ScheduleNextReminderOccurrenceCommand(UUID reminderId) {
}
