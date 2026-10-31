package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.FailRenewalPaymentCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.RenewalPaymentFailedHandlerImpl;
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

class RenewalPaymentFailedHandlerTest {

    private SubscriptionRepository subscriptionRepository;

    private RenewalPaymentFailedHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        handler =
                new RenewalPaymentFailedHandlerImpl(
                        subscriptionRepository
                );
    }

    @Test
    void shouldKeepSubscriptionUnchangedWhenRenewalPaymentFails() {

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

        Subscription result =
                handler.handle(
                        new FailRenewalPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_renewal_failed",
                                "Simulated renewal payment declined"
                        )
                );

        assertSame(
                subscription,
                result
        );

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
                        new FailRenewalPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_missing",
                                "Simulated renewal failure"
                        )
                )
        );
    }

    @Test
    void shouldRejectFailureForNonActiveSubscription() {

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
                        new FailRenewalPaymentCommand(
                                UUID.randomUUID(),
                                "pay_test_expired",
                                "Late renewal failure"
                        )
                )
        );
    }
}