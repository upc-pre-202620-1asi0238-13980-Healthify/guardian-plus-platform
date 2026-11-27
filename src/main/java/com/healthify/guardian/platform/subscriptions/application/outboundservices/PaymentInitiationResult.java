package com.healthify.guardian.platform.subscriptions.application.outboundservices;

import java.util.Objects;

public record PaymentInitiationResult(
        String provider,
        String externalReference,
        PaymentStatus status,
        String clientSecret
) {

    public PaymentInitiationResult(
            String provider,
            String externalReference,
            PaymentStatus status
    ) {
        this(provider, externalReference, status, null);
    }

    public PaymentInitiationResult {
        Objects.requireNonNull(provider, "provider must not be null");
        Objects.requireNonNull(externalReference, "externalReference must not be null");
        Objects.requireNonNull(status, "status must not be null");

        if (provider.isBlank()) {
            throw new IllegalArgumentException("provider must not be blank");
        }

        if (externalReference.isBlank()) {
            throw new IllegalArgumentException(
                    "externalReference must not be blank"
            );
        }
    }
}