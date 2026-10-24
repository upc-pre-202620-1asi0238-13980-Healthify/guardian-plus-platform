package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.ConfirmRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.RenewalPaymentConfirmedHandlerImpl;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
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

class RenewalPaymentConfirmedHandlerTest {

    private SubscriptionRepository subscriptionRepository;
    private PlanRepository planRepository;

    private RenewalPaymentConfirmedHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        planRepository =
                mock(PlanRepository.class);

        handler =
                new RenewalPaymentConfirmedHandlerImpl(
                        subscriptionRepository,
                        planRepository
                );
    }

    @Test
    void shouldRenewActiveMonthlySubscription() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        Instant currentPeriodEnd =
                Instant.parse(
                        "2026-11-23T19:00:00Z"
                );

        Subscription subscription =
                mock(Subscription.class);

        Plan plan =
                mock(Plan.class);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.ACTIVE
                );

        when(subscription.getPlanId())
                .thenReturn(
                        new PlanId(UUID.randomUUID())
                );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(currentPeriodEnd);

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(false);

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
                        new ConfirmRenewalPaymentCommand(
                                subscriptionUuid,
                                "pay_test_renewal_confirmed"
                        )
                );

        assertSame(
                subscription,
                result
        );

        verify(subscription)
                .renew(
                        eq(
                                Instant.parse(
                                        "2026-12-23T19:00:00Z"
                                )
                        ),
                        any(Instant.class)
                );

        verify(subscriptionRepository)
                .save(subscription);
    }

    @Test
    void shouldRenewActiveAnnualSubscription() {

        Subscription subscription =
                mock(Subscription.class);

        Plan plan =
                mock(Plan.class);

        Instant currentPeriodEnd =
                Instant.parse(
                        "2026-11-23T19:00:00Z"
                );

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.ACTIVE
                );

        when(subscription.getPlanId())
                .thenReturn(
                        new PlanId(UUID.randomUUID())
                );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(currentPeriodEnd);

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(false);

        when(plan.isActive())
                .thenReturn(true);

        when(plan.requiresPayment())
                .thenReturn(true);

        when(plan.getBillingCycle())
                .thenReturn(
                        BillingCycle.ANNUAL
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

        handler.handle(
                new ConfirmRenewalPaymentCommand(
                        UUID.randomUUID(),
                        "pay_test_annual"
                )
        );

        verify(subscription)
                .renew(
                        eq(
                                Instant.parse(
                                        "2027-11-23T19:00:00Z"
                                )
                        ),
                        any(Instant.class)
                );
    }

    @Test
    void shouldRejectRenewalWhenCancellationIsScheduled() {

        Subscription subscription =
                mock(Subscription.class);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.ACTIVE
                );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(
                        Instant.now().plusSeconds(86400)
                );

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(true);

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new ConfirmRenewalPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_cancelled"
                        )
                )
        );

        verifyNoInteractions(planRepository);

        verify(
                subscriptionRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldRejectRenewalForNonActiveSubscription() {

        Subscription subscription =
                mock(Subscription.class);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.EXPIRED
                );

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new ConfirmRenewalPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_expired"
                        )
                )
        );

        verifyNoInteractions(planRepository);
    }
}