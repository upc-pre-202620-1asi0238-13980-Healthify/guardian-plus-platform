package com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentProvider;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class SimulatedPaymentProviderAdapter implements PaymentProvider {

    private static final String PROVIDER_NAME = "stripe-simulator";
    private static final String REFERENCE_PREFIX = "pay_test_";

    @Override
    public PaymentInitiationResult initiatePayment(PaymentRequest request) {

        Objects.requireNonNull(request, "request must not be null");

        String externalReference =
                REFERENCE_PREFIX
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "");

        return new PaymentInitiationResult(
                PROVIDER_NAME,
                externalReference,
                PaymentStatus.PENDING
        );
    }
}