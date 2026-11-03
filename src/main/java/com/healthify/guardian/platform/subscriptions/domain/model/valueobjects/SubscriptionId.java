package com.healthify.guardian.platform.subscriptions.domain.model.valueobjects;

import java.util.UUID;

public record SubscriptionId(UUID value) {

    public SubscriptionId {
        if (value == null) {
            throw new IllegalArgumentException(
                    "subscription.id.invalid");
        }
    }

    public static SubscriptionId generate() {
        return new SubscriptionId(UUID.randomUUID());
    }
}