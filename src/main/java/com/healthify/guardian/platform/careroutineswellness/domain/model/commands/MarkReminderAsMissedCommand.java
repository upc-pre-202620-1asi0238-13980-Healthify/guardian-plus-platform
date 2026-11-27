package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to close an unconfirmed reminder as missed, once the family member has reviewed the omission.
 *
 * @param reminderId the reminder to close
 */
public record MarkReminderAsMissedCommand(UUID reminderId) {
}
