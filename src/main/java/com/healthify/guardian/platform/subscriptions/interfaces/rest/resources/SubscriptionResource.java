package com.healthify.guardian.platform.subscriptions.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record SubscriptionResource(
        UUID id,
        UUID subscriberId,
        UUID planId,
        String status,
        Instant currentPeriodStart,
        Instant currentPeriodEnd,
        boolean cancelAtPeriodEnd,
        Instant createdAt,
        Instant updatedAt
) {
}