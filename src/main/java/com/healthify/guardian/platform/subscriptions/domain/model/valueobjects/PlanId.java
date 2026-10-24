package com.healthify.guardian.platform.subscriptions.domain.model.valueobjects;

import java.util.UUID;

public record PlanId(UUID value) {

    public PlanId {
        if (value == null) {
            throw new IllegalArgumentException(
                    "subscription.plan-id.invalid");
        }
    }

    public static PlanId generate() {
        return new PlanId(UUID.randomUUID());
    }
}