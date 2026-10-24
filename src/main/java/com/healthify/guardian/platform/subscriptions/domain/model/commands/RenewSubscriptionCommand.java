package com.healthify.guardian.platform.subscriptions.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

public record RenewSubscriptionCommand(
        UUID subscriptionId,
        Instant newPeriodEnd
) {
}