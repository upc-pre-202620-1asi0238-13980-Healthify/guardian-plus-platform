package com.healthify.guardian.platform.subscriptions.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

public record ActivateSubscriptionCommand(
        UUID subscriptionId,
        UUID planId,
        Instant periodStart,
        Instant periodEnd
) {
}