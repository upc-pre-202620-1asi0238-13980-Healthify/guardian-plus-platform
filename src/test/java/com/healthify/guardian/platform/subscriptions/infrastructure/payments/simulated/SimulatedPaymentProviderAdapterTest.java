package com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated;

import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentProvider;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentPurpose;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
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
                PaymentPurpose.SUBSCRIPTION_RENEWAL,
                Instant.parse("2026-12-21T10:00:00Z")
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

    @Test
    void shouldGenerateSameIdempotencyKeyForActivationRetries() {

        UUID subscriptionId = UUID.randomUUID();
        UUID subscriberId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        PaymentRequest firstRequest = new PaymentRequest(
                subscriptionId,
                subscriberId,
                planId,
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_ACTIVATION
        );

        PaymentRequest repeatedRequest = new PaymentRequest(
                subscriptionId,
                subscriberId,
                planId,
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_ACTIVATION
        );

        assertEquals(
                firstRequest.idempotencyKey(),
                repeatedRequest.idempotencyKey()
        );

        assertEquals(
                "guardian-plus:activation:" + subscriptionId,
                firstRequest.idempotencyKey()
        );
    }

    @Test
    void shouldGenerateRenewalIdempotencyKeyForEachBillingPeriod() {

        UUID subscriptionId = UUID.randomUUID();
        UUID subscriberId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();

        Instant firstPeriodEnd =
                Instant.parse("2026-12-21T10:00:00Z");

        Instant secondPeriodEnd =
                Instant.parse("2027-01-21T10:00:00Z");

        PaymentRequest activationRequest = new PaymentRequest(
                subscriptionId,
                subscriberId,
                planId,
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_ACTIVATION
        );

        PaymentRequest firstRenewal = new PaymentRequest(
                subscriptionId,
                subscriberId,
                planId,
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_RENEWAL,
                firstPeriodEnd
        );

        PaymentRequest repeatedRenewal = new PaymentRequest(
                subscriptionId,
                subscriberId,
                planId,
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_RENEWAL,
                firstPeriodEnd
        );

        PaymentRequest secondRenewal = new PaymentRequest(
                subscriptionId,
                subscriberId,
                planId,
                new BigDecimal("29.90"),
                Currency.getInstance("PEN"),
                PaymentPurpose.SUBSCRIPTION_RENEWAL,
                secondPeriodEnd
        );

        assertEquals(
                firstRenewal.idempotencyKey(),
                repeatedRenewal.idempotencyKey()
        );

        assertNotEquals(
                firstRenewal.idempotencyKey(),
                secondRenewal.idempotencyKey()
        );

        assertNotEquals(
                activationRequest.idempotencyKey(),
                firstRenewal.idempotencyKey()
        );

        assertEquals(
                "guardian-plus:renewal:"
                        + subscriptionId
                        + ":"
                        + firstPeriodEnd,
                firstRenewal.idempotencyKey()
        );
    }

    @Test
    void shouldRejectRenewalWithoutCurrentPeriodEnd() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("29.90"),
                        Currency.getInstance("PEN"),
                        PaymentPurpose.SUBSCRIPTION_RENEWAL
                )
        );
    }

    @Test
    void shouldRejectActivationWithCurrentPeriodEnd() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new PaymentRequest(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new BigDecimal("29.90"),
                        Currency.getInstance("PEN"),
                        PaymentPurpose.SUBSCRIPTION_ACTIVATION,
                        Instant.parse("2026-12-21T10:00:00Z")
                )
        );
    }
}