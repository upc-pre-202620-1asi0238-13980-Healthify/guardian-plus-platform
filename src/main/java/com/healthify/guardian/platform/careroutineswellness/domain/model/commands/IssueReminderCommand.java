package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to issue a reminder that has just become due, applying
 * {@code ReminderIssuancePolicy}'s outcome.
 *
 * @param reminderId the reminder to issue
 */
public record IssueReminderCommand(UUID reminderId) {
}
