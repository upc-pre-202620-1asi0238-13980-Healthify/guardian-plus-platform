package com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;

import java.util.Objects;

public record SimulatedRenewalPaymentFlowResult(
        PaymentInitiationResult payment,
        SimulatedPaymentOutcome outcome,
        Subscription subscription
) {

    public SimulatedRenewalPaymentFlowResult {
        Objects.requireNonNull(
                payment,
                "payment must not be null"
        );

        Objects.requireNonNull(
                outcome,
                "outcome must not be null"
        );

        Objects.requireNonNull(
                subscription,
                "subscription must not be null"
        );
    }
}