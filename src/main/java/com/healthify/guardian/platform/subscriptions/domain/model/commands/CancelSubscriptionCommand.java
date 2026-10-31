package com.healthify.guardian.platform.subscriptions.domain.model.commands;

import java.util.UUID;

/**
 * Command to make the cancellation of a subscription effective.
 *
 * @param subscriptionId subscription to cancel
 */
public record CancelSubscriptionCommand(
        UUID subscriptionId
) {
}