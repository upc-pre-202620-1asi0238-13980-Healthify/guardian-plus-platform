package com.healthify.guardian.platform.subscriptions.application.commands;

import java.util.Objects;
import java.util.UUID;

public record FailRenewalPaymentCommand(
        UUID subscriptionId,
        String externalReference,
        String reason
) {

    public FailRenewalPaymentCommand {
        Objects.requireNonNull(
                subscriptionId,
                "subscriptionId must not be null"
        );

        Objects.requireNonNull(
                externalReference,
                "externalReference must not be null"
        );

        if (externalReference.isBlank()) {
            throw new IllegalArgumentException(
                    "externalReference must not be blank"
            );
        }
    }
}