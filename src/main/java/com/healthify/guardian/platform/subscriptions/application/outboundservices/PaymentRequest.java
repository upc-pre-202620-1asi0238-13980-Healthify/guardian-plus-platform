package com.healthify.guardian.platform.subscriptions.application.outboundservices;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record PaymentRequest(
        UUID subscriptionId,
        UUID subscriberId,
        UUID planId,
        BigDecimal amount,
        Currency currency,
        PaymentPurpose purpose
) {

    public PaymentRequest {
        Objects.requireNonNull(subscriptionId, "subscriptionId must not be null");
        Objects.requireNonNull(subscriberId, "subscriberId must not be null");
        Objects.requireNonNull(planId, "planId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        Objects.requireNonNull(purpose, "purpose must not be null");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
    }
}