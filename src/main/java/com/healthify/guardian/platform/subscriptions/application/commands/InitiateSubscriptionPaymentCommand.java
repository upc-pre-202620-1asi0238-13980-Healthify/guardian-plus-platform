package com.healthify.guardian.platform.subscriptions.application.commands;

import java.util.Objects;
import java.util.UUID;

public record InitiateSubscriptionPaymentCommand(
        UUID subscriptionId
) {

    public InitiateSubscriptionPaymentCommand {
        Objects.requireNonNull(
                subscriptionId,
                "subscriptionId must not be null"
        );
    }
}