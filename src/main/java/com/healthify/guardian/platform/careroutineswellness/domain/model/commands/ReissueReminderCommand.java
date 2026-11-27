package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to reissue a medication reminder that was not confirmed within the tolerance
 * window, as decided by the Reminder Reissue Policy.
 *
 * @param reminderId the reminder to reissue
 */
public record ReissueReminderCommand(UUID reminderId) {
}
