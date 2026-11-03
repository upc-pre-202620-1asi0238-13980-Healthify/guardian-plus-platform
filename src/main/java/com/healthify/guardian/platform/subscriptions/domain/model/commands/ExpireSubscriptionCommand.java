package com.healthify.guardian.platform.subscriptions.domain.model.commands;

import java.util.UUID;

/**
 * Command to expire a subscription.
 *
 * @param subscriptionId subscription to expire
 */
public record ExpireSubscriptionCommand(
        UUID subscriptionId
) {
}