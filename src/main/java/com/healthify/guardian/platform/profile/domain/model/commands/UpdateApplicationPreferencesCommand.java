package com.healthify.guardian.platform.profile.domain.model.commands;

import java.util.UUID;

/**
 * Command to update general application preferences.
 *
 * @param userId               the referenced IAM user
 * @param notificationsEnabled whether application notifications are enabled
 */
public record UpdateApplicationPreferencesCommand(
        UUID userId,
        boolean notificationsEnabled) {
}