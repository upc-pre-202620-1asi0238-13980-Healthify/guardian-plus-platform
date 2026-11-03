package com.healthify.guardian.platform.subscriptions.domain.model.valueobjects;

import java.util.UUID;

public record SubscriberId(UUID value) {

    public SubscriberId {
        if (value == null) {
            throw new IllegalArgumentException(
                    "subscription.subscriber-id.invalid");
        }
    }
}