package com.healthify.guardian.platform.subscriptions.application.commands;

import java.util.Objects;
import java.util.UUID;

public record InitiateRenewalPaymentCommand(
        UUID subscriptionId
) {

    public InitiateRenewalPaymentCommand {
        Objects.requireNonNull(
                subscriptionId,
                "subscriptionId must not be null"
        );
    }
}