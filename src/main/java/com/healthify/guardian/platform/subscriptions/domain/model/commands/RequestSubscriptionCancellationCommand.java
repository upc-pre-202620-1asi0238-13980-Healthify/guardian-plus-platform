package com.healthify.guardian.platform.subscriptions.domain.model.commands;

import java.util.UUID;

/**
 * Command to request the cancellation of a subscription.
 *
 * @param subscriptionId subscription to cancel
 */
public record RequestSubscriptionCancellationCommand(
        UUID subscriptionId
) {
}