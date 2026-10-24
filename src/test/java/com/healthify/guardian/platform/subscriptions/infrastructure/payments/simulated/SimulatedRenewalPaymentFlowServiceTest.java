package com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.FailRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.InitiateRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.InitiateRenewalPaymentHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentConfirmedHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.RenewalPaymentFailedHandler;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SimulatedRenewalPaymentFlowServiceTest {

    private InitiateRenewalPaymentHandler initiateRenewalPaymentHandler;
    private RenewalPaymentConfirmedHandler renewalPaymentConfirmedHandler;
    private RenewalPaymentFailedHandler renewalPaymentFailedHandler;

    private SimulatedRenewalPaymentFlowService service;

    @BeforeEach
    void setUp() {

        initiateRenewalPaymentHandler =
                mock(InitiateRenewalPaymentHandler.class);

        renewalPaymentConfirmedHandler =
                mock(RenewalPaymentConfirmedHandler.class);

        renewalPaymentFailedHandler =
                mock(RenewalPaymentFailedHandler.class);

        service =
                new SimulatedRenewalPaymentFlowService(
                        initiateRenewalPaymentHandler,
                        renewalPaymentConfirmedHandler,
                        renewalPaymentFailedHandler
                );
    }

    @Test
    void shouldCompleteConfirmedRenewalFlow() {

        UUID subscriptionId =
                UUID.randomUUID();

        Subscription subscription =
                mock(Subscription.class);

        PaymentInitiationResult payment =
                new PaymentInitiationResult(
                        "stripe-simulator",
                        "pay_test_renewal_confirmed",
                        PaymentStatus.PENDING
                );

        when(
                initiateRenewalPaymentHandler.handle(
                        any(InitiateRenewalPaymentCommand.class)
                )
        ).thenReturn(payment);

        when(
                renewalPaymentConfirmedHandler.handle(
                        any(ConfirmRenewalPaymentCommand.class)
                )
        ).thenReturn(subscription);

        SimulatedRenewalPaymentFlowResult result =
                service.process(
                        subscriptionId,
                        SimulatedPaymentOutcome.CONFIRMED
                );

        assertNotNull(result);

        assertSame(
                payment,
                result.payment()
        );

        assertSame(
                subscription,
                result.subscription()
        );

        assertEquals(
                SimulatedPaymentOutcome.CONFIRMED,
                result.outcome()
        );

        verify(initiateRenewalPaymentHandler)
                .handle(
                        any(InitiateRenewalPaymentCommand.class)
                );

        verify(renewalPaymentConfirmedHandler)
                .handle(
                        any(ConfirmRenewalPaymentCommand.class)
                );

        verifyNoInteractions(
                renewalPaymentFailedHandler
        );
    }

    @Test
    void shouldCompleteFailedRenewalFlow() {

        UUID subscriptionId =
                UUID.randomUUID();

        Subscription subscription =
                mock(Subscription.class);

        PaymentInitiationResult payment =
                new PaymentInitiationResult(
                        "stripe-simulator",
                        "pay_test_renewal_failed",
                        PaymentStatus.PENDING
                );

        when(
                initiateRenewalPaymentHandler.handle(
                        any(InitiateRenewalPaymentCommand.class)
                )
        ).thenReturn(payment);

        when(
                renewalPaymentFailedHandler.handle(
                        any(FailRenewalPaymentCommand.class)
                )
        ).thenReturn(subscription);

        SimulatedRenewalPaymentFlowResult result =
                service.process(
                        subscriptionId,
                        SimulatedPaymentOutcome.FAILED
                );

        assertNotNull(result);

        assertSame(
                payment,
                result.payment()
        );

        assertSame(
                subscription,
                result.subscription()
        );

        assertEquals(
                SimulatedPaymentOutcome.FAILED,
                result.outcome()
        );

        verify(initiateRenewalPaymentHandler)
                .handle(
                        any(InitiateRenewalPaymentCommand.class)
                );

        verify(renewalPaymentFailedHandler)
                .handle(
                        any(FailRenewalPaymentCommand.class)
                );

        verifyNoInteractions(
                renewalPaymentConfirmedHandler
        );
    }

    @Test
    void shouldRejectNullRenewalOutcome() {

        assertThrows(
                NullPointerException.class,
                () -> service.process(
                        UUID.randomUUID(),
                        null
                )
        );

        verifyNoInteractions(
                initiateRenewalPaymentHandler,
                renewalPaymentConfirmedHandler,
                renewalPaymentFailedHandler
        );
    }

    @Test
    void shouldRejectUnexpectedInitialRenewalPaymentStatus() {

        UUID subscriptionId =
                UUID.randomUUID();

        PaymentInitiationResult payment =
                new PaymentInitiationResult(
                        "stripe-simulator",
                        "pay_test_invalid_renewal",
                        PaymentStatus.CONFIRMED
                );

        when(
                initiateRenewalPaymentHandler.handle(
                        any(InitiateRenewalPaymentCommand.class)
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
                renewalPaymentConfirmedHandler,
                renewalPaymentFailedHandler
        );
    }
}