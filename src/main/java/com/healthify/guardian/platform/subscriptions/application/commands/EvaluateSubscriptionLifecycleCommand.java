package com.healthify.guardian.platform.subscriptions.application.commands;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EvaluateSubscriptionLifecycleCommand(
        UUID subscriptionId,
        Instant evaluatedAt,
        boolean renewalAttemptFailed
) {

    public EvaluateSubscriptionLifecycleCommand {

        Objects.requireNonNull(
                subscriptionId,
                "subscriptionId must not be null"
        );

        Objects.requireNonNull(
                evaluatedAt,
                "evaluatedAt must not be null"
        );
    }
}