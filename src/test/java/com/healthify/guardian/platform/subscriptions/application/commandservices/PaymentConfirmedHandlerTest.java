package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.PaymentConfirmedHandlerImpl;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.SyncEntitlementsFromPlanCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PaymentConfirmedHandlerTest {

    private SubscriptionRepository subscriptionRepository;
    private PlanRepository planRepository;
    private EntitlementSetCommandService entitlementSetCommandService;

    private PaymentConfirmedHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        planRepository =
                mock(PlanRepository.class);

        entitlementSetCommandService =
                mock(EntitlementSetCommandService.class);

        handler =
                new PaymentConfirmedHandlerImpl(
                        subscriptionRepository,
                        planRepository,
                        entitlementSetCommandService
                );
    }

    @Test
    void shouldActivateSubscriptionWhenPaymentIsConfirmed() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        Subscription subscription =
                mock(Subscription.class);

        Plan plan =
                mock(Plan.class);

        PlanId planId =
                new PlanId(planUuid);

        when(subscription.getId())
                .thenReturn(
                        new SubscriptionId(subscriptionUuid)
                );

        when(subscription.getPlanId())
                .thenReturn(planId);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.PENDING
                );

        when(plan.isActive())
                .thenReturn(true);

        when(plan.requiresPayment())
                .thenReturn(true);

        when(plan.getBillingCycle())
                .thenReturn(
                        BillingCycle.MONTHLY
                );

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        when(
                planRepository.findById(
                        any(PlanId.class)
                )
        ).thenReturn(Optional.of(plan));

        when(
                subscriptionRepository.save(subscription)
        ).thenReturn(subscription);

        Subscription result =
                handler.handle(
                        new ConfirmSubscriptionPaymentCommand(
                                subscriptionUuid,
                                "pay_test_123"
                        )
                );

        assertSame(subscription, result);

        verify(subscription)
                .activate(
                        eq(planId),
                        any(Instant.class),
                        any(Instant.class),
                        any(Instant.class)
                );

        verify(entitlementSetCommandService)
                .handle(
                        any(SyncEntitlementsFromPlanCommand.class)
                );

        verify(subscriptionRepository)
                .save(subscription);
    }

    @Test
    void shouldRejectConfirmationWhenSubscriptionDoesNotExist() {

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new ConfirmSubscriptionPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_missing"
                        )
                )
        );

        verifyNoInteractions(
                planRepository,
                entitlementSetCommandService
        );
    }

    @Test
    void shouldRejectConfirmationWhenSubscriptionIsAlreadyActive() {

        Subscription subscription =
                mock(Subscription.class);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.ACTIVE
                );

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new ConfirmSubscriptionPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_active"
                        )
                )
        );

        verifyNoInteractions(
                planRepository,
                entitlementSetCommandService
        );

        verify(
                subscriptionRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldRejectConfirmationWhenPlanIsInactive() {

        Subscription subscription =
                mock(Subscription.class);

        Plan plan =
                mock(Plan.class);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.PENDING
                );

        when(subscription.getPlanId())
                .thenReturn(
                        new PlanId(UUID.randomUUID())
                );

        when(plan.isActive())
                .thenReturn(false);

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        when(
                planRepository.findById(
                        any(PlanId.class)
                )
        ).thenReturn(Optional.of(plan));

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new ConfirmSubscriptionPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_inactive"
                        )
                )
        );

        verifyNoInteractions(
                entitlementSetCommandService
        );

        verify(
                subscriptionRepository,
                never()
        ).save(any());
    }
}