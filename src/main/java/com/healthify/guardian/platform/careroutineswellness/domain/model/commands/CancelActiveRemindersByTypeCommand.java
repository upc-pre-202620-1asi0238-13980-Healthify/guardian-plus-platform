package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.util.UUID;

/**
 * Command to cancel every still-active reminder of one type for a person under care, e.g. when the
 * hydration plan is turned off or its interval changes.
 *
 * @param personUnderCareId the person whose reminders must be cancelled
 * @param type              the reminder type to cancel
 */
public record CancelActiveRemindersByTypeCommand(UUID personUnderCareId, ReminderType type) {
}
