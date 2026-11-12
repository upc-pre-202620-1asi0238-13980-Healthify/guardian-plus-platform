package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to cancel a reminder that is still scheduled, issued or reissued.
 *
 * @param reminderId the reminder to cancel
 */
public record CancelReminderCommand(UUID reminderId) {
}
