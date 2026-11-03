package com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentProvider;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentPurpose;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SimulatedPaymentProviderAdapterTest {

    private final PaymentProvider paymentProvider =
            new SimulatedPaymentProviderAdapter();

    @Test
    void shouldInitiateSubscriptionPaymentAsPending() {

        PaymentRequest request = new PaymentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_ACTIVATION
        );

        PaymentInitiationResult result =
                paymentProvider.initiatePayment(request);

        assertNotNull(result);

        assertEquals(
                "stripe-simulator",
                result.provider()
        );

        assertEquals(
                PaymentStatus.PENDING,
                result.status()
        );

        assertNotNull(result.externalReference());

        assertTrue(
                result.externalReference().startsWith("pay_test_")
        );
    }

    @Test
    void shouldGenerateDifferentReferencesForDifferentPayments() {

        PaymentRequest firstRequest = new PaymentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_ACTIVATION
        );

        PaymentRequest secondRequest = new PaymentRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_RENEWAL
        );

        PaymentInitiationResult firstResult =
                paymentProvider.initiatePayment(firstRequest);

        PaymentInitiationResult secondResult =
                paymentProvider.initiatePayment(secondRequest);

        assertNotEquals(
                firstResult.externalReference(),
                secondResult.externalReference()
        );
    }

    @Test
    void shouldRejectNegativePaymentAmount() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("-1.00"),
                        Currency.getInstance("PEN"),
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION
                )
        );
    }

    @Test
    void shouldRejectNullPaymentRequest() {

        assertThrows(
                NullPointerException.class,
                () -> paymentProvider.initiatePayment(null)
        );
    }
}