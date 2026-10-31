package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;

import java.util.UUID;

/**
 * Command to enable or disable a notification channel for a Care Circle member.
 *
 * @param userId      the Care Circle member
 * @param channel     the channel being configured
 * @param enabled     whether the channel is enabled
 * @param deviceToken the push device token; only meaningful for the {@code PUSH} channel
 */
public record ConfigureAlertChannelCommand(UUID userId, NotificationChannel channel, Boolean enabled, String deviceToken) {
}
