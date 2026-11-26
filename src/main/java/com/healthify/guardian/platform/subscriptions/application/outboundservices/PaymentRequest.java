package com.healthify.guardian.platform.subscriptions.application.outboundservices;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Objects;
import java.util.UUID;

public record PaymentRequest(
        UUID subscriptionId,
        UUID subscriberId,
        UUID planId,
        BigDecimal amount,
        Currency currency,
        PaymentPurpose purpose,
        Instant currentPeriodEnd
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

        if (purpose == PaymentPurpose.SUBSCRIPTION_RENEWAL
                && currentPeriodEnd == null) {

            throw new IllegalArgumentException(
                    "currentPeriodEnd is required for renewal payments"
            );
        }

        if (purpose == PaymentPurpose.SUBSCRIPTION_ACTIVATION
                && currentPeriodEnd != null) {

            throw new IllegalArgumentException(
                    "currentPeriodEnd must be null for activation payments"
            );
        }
    }

    public PaymentRequest(
            UUID subscriptionId,
            UUID subscriberId,
            UUID planId,
            BigDecimal amount,
            Currency currency,
            PaymentPurpose purpose
    ) {
        this(
                subscriptionId,
                subscriberId,
                planId,
                amount,
                currency,
                purpose,
                null
        );
    }

    public String idempotencyKey() {

        return switch (purpose) {

            case SUBSCRIPTION_ACTIVATION ->
                    "guardian-plus:activation:"
                            + subscriptionId;

            case SUBSCRIPTION_RENEWAL ->
                    "guardian-plus:renewal:"
                            + subscriptionId
                            + ":"
                            + currentPeriodEnd;
        };
    }
}