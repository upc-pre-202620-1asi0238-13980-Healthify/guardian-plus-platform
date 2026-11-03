package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.InitiateRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.InitiateRenewalPaymentHandlerImpl;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentProvider;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentPurpose;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentRequest;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentStatus;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.Money;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InitiateRenewalPaymentHandlerTest {

    private SubscriptionRepository subscriptionRepository;
    private PlanRepository planRepository;
    private PaymentProvider paymentProvider;

    private InitiateRenewalPaymentHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        planRepository =
                mock(PlanRepository.class);

        paymentProvider =
                mock(PaymentProvider.class);

        handler =
                new InitiateRenewalPaymentHandlerImpl(
                        subscriptionRepository,
                        planRepository,
                        paymentProvider
                );
    }

    @Test
    void shouldInitiateRenewalPaymentForActiveSubscription() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID subscriberUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        Subscription subscription =
                mock(Subscription.class);

        Plan plan =
                mock(Plan.class);

        when(subscription.getId())
                .thenReturn(
                        new SubscriptionId(subscriptionUuid)
                );

        when(subscription.getSubscriberId())
                .thenReturn(
                        new SubscriberId(subscriberUuid)
                );

        when(subscription.getPlanId())
                .thenReturn(
                        new PlanId(planUuid)
                );

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.ACTIVE
                );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(
                        Instant.now().plusSeconds(86400)
                );

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(false);

        when(plan.isActive())
                .thenReturn(true);

        when(plan.requiresPayment())
                .thenReturn(true);

        when(plan.getPrice())
                .thenReturn(
                        new Money(
                                new BigDecimal("29.90"),
                                Currency.getInstance("PEN")
                        )
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
                paymentProvider.initiatePayment(
                        any(PaymentRequest.class)
                )
        ).thenReturn(
                new PaymentInitiationResult(
                        "stripe-simulator",
                        "pay_test_renewal",
                        PaymentStatus.PENDING
                )
        );

        PaymentInitiationResult result =
                handler.handle(
                        new InitiateRenewalPaymentCommand(
                                subscriptionUuid
                        )
                );

        assertEquals(
                PaymentStatus.PENDING,
                result.status()
        );

        assertEquals(
                "pay_test_renewal",
                result.externalReference()
        );

        ArgumentCaptor<PaymentRequest> captor =
                ArgumentCaptor.forClass(
                        PaymentRequest.class
                );

        verify(paymentProvider)
                .initiatePayment(captor.capture());

        PaymentRequest request =
                captor.getValue();

        assertEquals(
                PaymentPurpose.SUBSCRIPTION_RENEWAL,
                request.purpose()
        );

        assertEquals(
                subscriptionUuid,
                request.subscriptionId()
        );

        assertEquals(
                subscriberUuid,
                request.subscriberId()
        );

        assertEquals(
                planUuid,
                request.planId()
        );

        assertEquals(
                new BigDecimal("29.90"),
                request.amount()
        );

        assertEquals(
                Currency.getInstance("PEN"),
                request.currency()
        );
    }

    @Test
    void shouldRejectRenewalWhenSubscriptionDoesNotExist() {

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new InitiateRenewalPaymentCommand(
                                UUID.randomUUID()
                        )
                )
        );

        verifyNoInteractions(
                planRepository,
                paymentProvider
        );
    }

    @Test
    void shouldRejectRenewalForPendingSubscription() {

        Subscription subscription =
                mock(Subscription.class);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.PENDING
                );

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new InitiateRenewalPaymentCommand(
                                UUID.randomUUID()
                        )
                )
        );

        verifyNoInteractions(
                planRepository,
                paymentProvider
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
                        new InitiateRenewalPaymentCommand(
                                UUID.randomUUID()
                        )
                )
        );

        verifyNoInteractions(
                planRepository,
                paymentProvider
        );
    }

    @Test
    void shouldRejectRenewalForFreePlan() {

        Subscription subscription =
                mock(Subscription.class);

        Plan plan =
                mock(Plan.class);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.ACTIVE
                );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(
                        Instant.now().plusSeconds(86400)
                );

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(false);

        when(subscription.getPlanId())
                .thenReturn(
                        new PlanId(UUID.randomUUID())
                );

        when(plan.isActive())
                .thenReturn(true);

        when(plan.requiresPayment())
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
                        new InitiateRenewalPaymentCommand(
                                UUID.randomUUID()
                        )
                )
        );

        verifyNoInteractions(paymentProvider);
    }
}