package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to confirm a reminder that has been issued (or reissued) to the person under care.
 *
 * @param reminderId the reminder to confirm
 */
public record ConfirmReminderCommand(UUID reminderId) {
}
