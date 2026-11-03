package com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.InitiateSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.InitiateSubscriptionPaymentHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.PaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SimulatedPaymentFlowServiceTest {

    private InitiateSubscriptionPaymentHandler initiatePaymentHandler;
    private PaymentConfirmedHandler paymentConfirmedHandler;
    private PaymentFailedHandler paymentFailedHandler;

    private SimulatedPaymentFlowService service;

    @BeforeEach
    void setUp() {

        initiatePaymentHandler =
                mock(InitiateSubscriptionPaymentHandler.class);

        paymentConfirmedHandler =
                mock(PaymentConfirmedHandler.class);

        paymentFailedHandler =
                mock(PaymentFailedHandler.class);

        service =
                new SimulatedPaymentFlowService(
                        initiatePaymentHandler,
                        paymentConfirmedHandler,
                        paymentFailedHandler
                );
    }

    @Test
    void shouldCompleteConfirmedPaymentFlow() {

        UUID subscriptionId =
                UUID.randomUUID();

        Subscription subscription =
                mock(Subscription.class);

        PaymentInitiationResult payment =
                new PaymentInitiationResult(
                        "stripe-simulator",
                        "pay_test_confirmed",
                        PaymentStatus.PENDING
                );

        when(
                initiatePaymentHandler.handle(
                        any(InitiateSubscriptionPaymentCommand.class)
                )
        ).thenReturn(payment);

        when(
                paymentConfirmedHandler.handle(
                        any(ConfirmSubscriptionPaymentCommand.class)
                )
        ).thenReturn(subscription);

        SimulatedPaymentFlowResult result =
                service.process(
                        subscriptionId,
                        SimulatedPaymentOutcome.CONFIRMED
                );

        assertNotNull(result);

        assertSame(
                subscription,
                result.subscription()
        );

        assertSame(
                payment,
                result.payment()
        );

        assertEquals(
                SimulatedPaymentOutcome.CONFIRMED,
                result.outcome()
        );

        verify(initiatePaymentHandler)
                .handle(
                        any(InitiateSubscriptionPaymentCommand.class)
                );

        verify(paymentConfirmedHandler)
                .handle(
                        any(ConfirmSubscriptionPaymentCommand.class)
                );

        verifyNoInteractions(paymentFailedHandler);
    }

    @Test
    void shouldCompleteFailedPaymentFlow() {

        UUID subscriptionId =
                UUID.randomUUID();

        Subscription subscription =
                mock(Subscription.class);

        PaymentInitiationResult payment =
                new PaymentInitiationResult(
                        "stripe-simulator",
                        "pay_test_failed",
                        PaymentStatus.PENDING
                );

        when(
                initiatePaymentHandler.handle(
                        any(InitiateSubscriptionPaymentCommand.class)
                )
        ).thenReturn(payment);

        when(
                paymentFailedHandler.handle(
                        any(FailSubscriptionPaymentCommand.class)
                )
        ).thenReturn(subscription);

        SimulatedPaymentFlowResult result =
                service.process(
                        subscriptionId,
                        SimulatedPaymentOutcome.FAILED
                );

        assertNotNull(result);

        assertSame(
                subscription,
                result.subscription()
        );

        assertSame(
                payment,
                result.payment()
        );

        assertEquals(
                SimulatedPaymentOutcome.FAILED,
                result.outcome()
        );

        verify(initiatePaymentHandler)
                .handle(
                        any(InitiateSubscriptionPaymentCommand.class)
                );

        verify(paymentFailedHandler)
                .handle(
                        any(FailSubscriptionPaymentCommand.class)
                );

        verifyNoInteractions(paymentConfirmedHandler);
    }

    @Test
    void shouldRejectNullOutcome() {

        assertThrows(
                NullPointerException.class,
                () -> service.process(
                        UUID.randomUUID(),
                        null
                )
        );

        verifyNoInteractions(
                initiatePaymentHandler,
                paymentConfirmedHandler,
                paymentFailedHandler
        );
    }

    @Test
    void shouldRejectUnexpectedInitialPaymentStatus() {

        UUID subscriptionId =
                UUID.randomUUID();

        PaymentInitiationResult payment =
                new PaymentInitiationResult(
                        "stripe-simulator",
                        "pay_test_invalid",
                        PaymentStatus.CONFIRMED
                );

        when(
                initiatePaymentHandler.handle(
                        any(InitiateSubscriptionPaymentCommand.class)
                )
        ).thenReturn(payment);

        assertThrows(
                IllegalStateException.class,
                () -> service.process(
                        subscriptionId,
                        SimulatedPaymentOutcome.CONFIRMED
                )
        );

        verifyNoInteractions(
                paymentConfirmedHandler,
                paymentFailedHandler
        );
    }
}