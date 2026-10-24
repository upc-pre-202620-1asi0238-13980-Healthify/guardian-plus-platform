package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.InitiateSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.InitiateSubscriptionPaymentHandlerImpl;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentInitiationResult;
import com.healthify.guardian.platform.subscriptions.application.outboundservices.PaymentProvider;
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
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InitiateSubscriptionPaymentHandlerTest {

    private SubscriptionRepository subscriptionRepository;
    private PlanRepository planRepository;
    private PaymentProvider paymentProvider;

    private InitiateSubscriptionPaymentHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        planRepository =
                mock(PlanRepository.class);

        paymentProvider =
                mock(PaymentProvider.class);

        handler =
                new InitiateSubscriptionPaymentHandlerImpl(
                        subscriptionRepository,
                        planRepository,
                        paymentProvider
                );
    }

    @Test
    void shouldInitiatePaymentForPendingPaidSubscription() {

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
                        SubscriptionStatus.PENDING
                );

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
                        "pay_test_123",
                        PaymentStatus.PENDING
                )
        );

        PaymentInitiationResult result =
                handler.handle(
                        new InitiateSubscriptionPaymentCommand(
                                subscriptionUuid
                        )
                );

        assertNotNull(result);

        assertEquals(
                "stripe-simulator",
                result.provider()
        );

        assertEquals(
                "pay_test_123",
                result.externalReference()
        );

        assertEquals(
                PaymentStatus.PENDING,
                result.status()
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
    void shouldRejectPaymentWhenSubscriptionDoesNotExist() {

        UUID subscriptionId =
                UUID.randomUUID();

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new InitiateSubscriptionPaymentCommand(
                                subscriptionId
                        )
                )
        );

        verifyNoInteractions(
                planRepository,
                paymentProvider
        );
    }

    @Test
    void shouldRejectPaymentWhenSubscriptionIsNotPending() {

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
                        new InitiateSubscriptionPaymentCommand(
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
    void shouldRejectPaymentWhenPlanIsInactive() {

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
                        new PlanId(
                                UUID.randomUUID()
                        )
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
                        new InitiateSubscriptionPaymentCommand(
                                UUID.randomUUID()
                        )
                )
        );

        verifyNoInteractions(paymentProvider);
    }

    @Test
    void shouldRejectPaymentForFreePlan() {

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
                        new PlanId(
                                UUID.randomUUID()
                        )
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
                        new InitiateSubscriptionPaymentCommand(
                                UUID.randomUUID()
                        )
                )
        );

        verifyNoInteractions(paymentProvider);
    }
}