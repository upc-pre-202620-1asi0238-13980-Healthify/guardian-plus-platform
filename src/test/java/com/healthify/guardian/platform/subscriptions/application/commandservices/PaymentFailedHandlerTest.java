package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.FailSubscriptionPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.PaymentFailedHandlerImpl;
import com.healthify.guardian.platform.subscriptions.application.exceptions.SubscriptionPaymentException;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentFailedHandlerTest {

    private SubscriptionRepository subscriptionRepository;

    private PaymentFailedHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        handler =
                new PaymentFailedHandlerImpl(
                        subscriptionRepository
                );
    }

    @Test
    void shouldKeepSubscriptionPendingWhenPaymentFails() {

        UUID subscriptionUuid =
                UUID.randomUUID();

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

        Subscription result =
                handler.handle(
                        new FailSubscriptionPaymentCommand(
                                subscriptionUuid,
                                "pay_test_failed",
                                "Simulated payment declined"
                        )
                );

        assertSame(subscription, result);

        verify(
                subscriptionRepository,
                never()
        ).save(any());
    }

    @Test
    void shouldRejectFailureWhenSubscriptionDoesNotExist() {

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                SubscriptionPaymentException.class,
                () -> handler.handle(
                        new FailSubscriptionPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_missing",
                                "Simulated failure"
                        )
                )
        );
    }

    @Test
    void shouldRejectFailureForNonPendingSubscription() {

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
                        new FailSubscriptionPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_active",
                                "Late failure"
                        )
                )
        );
    }
}