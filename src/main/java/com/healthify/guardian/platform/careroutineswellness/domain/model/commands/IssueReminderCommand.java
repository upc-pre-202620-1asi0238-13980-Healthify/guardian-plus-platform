package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to issue a reminder that has just become due, applying
 * the Reminder Issuance Policy (the reminder may end up suppressed instead).
 *
 * @param reminderId the reminder to issue
 */
public record IssueReminderCommand(UUID reminderId) {
}
