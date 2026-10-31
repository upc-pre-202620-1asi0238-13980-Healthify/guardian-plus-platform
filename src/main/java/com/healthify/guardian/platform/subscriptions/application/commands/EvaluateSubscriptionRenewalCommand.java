package com.healthify.guardian.platform.subscriptions.application.commands;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EvaluateSubscriptionRenewalCommand(
        UUID subscriptionId,
        Instant evaluatedAt
) {

    public EvaluateSubscriptionRenewalCommand {

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