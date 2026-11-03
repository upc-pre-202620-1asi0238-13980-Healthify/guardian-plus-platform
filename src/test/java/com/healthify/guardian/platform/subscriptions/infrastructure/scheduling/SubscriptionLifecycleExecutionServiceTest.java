package com.healthify.guardian.platform.subscriptions.infrastructure.scheduling;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionLifecycleCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EvaluateSubscriptionLifecycleHandler;
import com.healthify.guardian.platform.subscriptions.application.commandservices.SubscriptionCommandService;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.SubscriptionLifecycleAction;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.CancelSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ExpireSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated.SimulatedPaymentOutcome;
import com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated.SimulatedRenewalPaymentFlowResult;
import com.healthify.guardian.platform.subscriptions.infrastructure.payments.simulated.SimulatedRenewalPaymentFlowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionLifecycleExecutionServiceTest {

    private EvaluateSubscriptionLifecycleHandler lifecycleHandler;
    private SimulatedRenewalPaymentFlowService renewalPaymentFlowService;
    private SubscriptionCommandService subscriptionCommandService;

    private SubscriptionLifecycleExecutionService service;

    @BeforeEach
    void setUp() {

        lifecycleHandler =
                mock(EvaluateSubscriptionLifecycleHandler.class);

        renewalPaymentFlowService =
                mock(SimulatedRenewalPaymentFlowService.class);

        subscriptionCommandService =
                mock(SubscriptionCommandService.class);

        service =
                new SubscriptionLifecycleExecutionService(
                        lifecycleHandler,
                        renewalPaymentFlowService,
                        subscriptionCommandService
                );
    }

    @Test
    void shouldDoNothingWhenNoLifecycleActionIsDue() {

        when(
                lifecycleHandler.handle(
                        any(EvaluateSubscriptionLifecycleCommand.class)
                )
        ).thenReturn(
                SubscriptionLifecycleAction.NONE
        );

        SubscriptionLifecycleExecutionResult result =
                service.execute(
                        UUID.randomUUID(),
                        Instant.now(),
                        SimulatedPaymentOutcome.CONFIRMED
                );

        assertEquals(
                SubscriptionLifecycleExecutionResult.NONE,
                result
        );

        verifyNoInteractions(
                renewalPaymentFlowService,
                subscriptionCommandService
        );
    }

    @Test
    void shouldCancelSubscriptionWhenCancellationIsDue() {

        when(
                lifecycleHandler.handle(
                        any(EvaluateSubscriptionLifecycleCommand.class)
                )
        ).thenReturn(
                SubscriptionLifecycleAction.CANCELLATION_DUE
        );

        SubscriptionLifecycleExecutionResult result =
                service.execute(
                        UUID.randomUUID(),
                        Instant.now(),
                        SimulatedPaymentOutcome.CONFIRMED
                );

        assertEquals(
                SubscriptionLifecycleExecutionResult.CANCELLED,
                result
        );

        verify(subscriptionCommandService)
                .handle(
                        any(CancelSubscriptionCommand.class)
                );

        verifyNoInteractions(
                renewalPaymentFlowService
        );
    }

    @Test
    void shouldRenewSubscriptionWhenRenewalPaymentIsConfirmed() {

        when(
                lifecycleHandler.handle(
                        any(EvaluateSubscriptionLifecycleCommand.class)
                )
        ).thenReturn(
                SubscriptionLifecycleAction.RENEWAL_DUE
        );

        SubscriptionLifecycleExecutionResult result =
                service.execute(
                        UUID.randomUUID(),
                        Instant.now(),
                        SimulatedPaymentOutcome.CONFIRMED
                );

        assertEquals(
                SubscriptionLifecycleExecutionResult.RENEWED,
                result
        );

        verify(renewalPaymentFlowService)
                .process(
                        any(UUID.class),
                        eq(SimulatedPaymentOutcome.CONFIRMED)
                );

        verifyNoInteractions(
                subscriptionCommandService
        );
    }

    @Test
    void shouldExpireSubscriptionAfterFailedRenewal() {

        when(
                lifecycleHandler.handle(
                        any(EvaluateSubscriptionLifecycleCommand.class)
                )
        ).thenReturn(
                SubscriptionLifecycleAction.RENEWAL_DUE,
                SubscriptionLifecycleAction.EXPIRATION_DUE
        );

        SubscriptionLifecycleExecutionResult result =
                service.execute(
                        UUID.randomUUID(),
                        Instant.now(),
                        SimulatedPaymentOutcome.FAILED
                );

        assertEquals(
                SubscriptionLifecycleExecutionResult.EXPIRED,
                result
        );

        verify(renewalPaymentFlowService)
                .process(
                        any(UUID.class),
                        eq(SimulatedPaymentOutcome.FAILED)
                );

        verify(
                lifecycleHandler,
                times(2)
        ).handle(
                any(EvaluateSubscriptionLifecycleCommand.class)
        );

        verify(subscriptionCommandService)
                .handle(
                        any(ExpireSubscriptionCommand.class)
                );
    }

    @Test
    void shouldKeepRenewalFailedWhenExpirationIsNotDue() {

        when(
                lifecycleHandler.handle(
                        any(EvaluateSubscriptionLifecycleCommand.class)
                )
        ).thenReturn(
                SubscriptionLifecycleAction.RENEWAL_DUE,
                SubscriptionLifecycleAction.NONE
        );

        SubscriptionLifecycleExecutionResult result =
                service.execute(
                        UUID.randomUUID(),
                        Instant.now(),
                        SimulatedPaymentOutcome.FAILED
                );

        assertEquals(
                SubscriptionLifecycleExecutionResult.RENEWAL_FAILED,
                result
        );

        verify(renewalPaymentFlowService)
                .process(
                        any(UUID.class),
                        eq(SimulatedPaymentOutcome.FAILED)
                );

        verify(
                subscriptionCommandService,
                never()
        ).handle(
                any(ExpireSubscriptionCommand.class)
        );
    }
}