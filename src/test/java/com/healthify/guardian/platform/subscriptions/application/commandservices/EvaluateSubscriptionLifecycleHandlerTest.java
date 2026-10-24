package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionExpirationCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionLifecycleCommand;
import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionRenewalCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.EvaluateSubscriptionLifecycleHandlerImpl;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.ExpirationEvaluation;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.RenewalEvaluation;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.SubscriptionLifecycleAction;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EvaluateSubscriptionLifecycleHandlerTest {

    private SubscriptionRepository subscriptionRepository;
    private EvaluateSubscriptionRenewalHandler renewalHandler;
    private EvaluateSubscriptionExpirationHandler expirationHandler;

    private EvaluateSubscriptionLifecycleHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        renewalHandler =
                mock(EvaluateSubscriptionRenewalHandler.class);

        expirationHandler =
                mock(EvaluateSubscriptionExpirationHandler.class);

        handler =
                new EvaluateSubscriptionLifecycleHandlerImpl(
                        subscriptionRepository,
                        renewalHandler,
                        expirationHandler
                );
    }

    @Test
    void shouldReturnRenewalDueForExpiredActiveSubscription() {

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

        when(
                renewalHandler.handle(
                        any(EvaluateSubscriptionRenewalCommand.class)
                )
        ).thenReturn(
                RenewalEvaluation.DUE
        );

        SubscriptionLifecycleAction result =
                handler.handle(
                        new EvaluateSubscriptionLifecycleCommand(
                                UUID.randomUUID(),
                                Instant.now(),
                                false
                        )
                );

        assertEquals(
                SubscriptionLifecycleAction.RENEWAL_DUE,
                result
        );

        verifyNoInteractions(expirationHandler);
    }

    @Test
    void shouldReturnExpirationDueAfterFailedRenewal() {

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

        when(
                renewalHandler.handle(
                        any(EvaluateSubscriptionRenewalCommand.class)
                )
        ).thenReturn(
                RenewalEvaluation.DUE
        );

        when(
                expirationHandler.handle(
                        any(EvaluateSubscriptionExpirationCommand.class)
                )
        ).thenReturn(
                ExpirationEvaluation.DUE
        );

        SubscriptionLifecycleAction result =
                handler.handle(
                        new EvaluateSubscriptionLifecycleCommand(
                                UUID.randomUUID(),
                                Instant.now(),
                                true
                        )
                );

        assertEquals(
                SubscriptionLifecycleAction.EXPIRATION_DUE,
                result
        );
    }

    @Test
    void shouldReturnCancellationDueWhenScheduledPeriodEnded() {

        Subscription subscription =
                mock(Subscription.class);

        Instant evaluatedAt =
                Instant.parse(
                        "2026-10-23T20:00:00Z"
                );

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.CANCELLATION_SCHEDULED
                );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(
                        Instant.parse(
                                "2026-10-23T19:00:00Z"
                        )
                );

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        SubscriptionLifecycleAction result =
                handler.handle(
                        new EvaluateSubscriptionLifecycleCommand(
                                UUID.randomUUID(),
                                evaluatedAt,
                                false
                        )
                );

        assertEquals(
                SubscriptionLifecycleAction.CANCELLATION_DUE,
                result
        );

        verifyNoInteractions(
                renewalHandler,
                expirationHandler
        );
    }

    @Test
    void shouldDoNothingWhenScheduledCancellationIsNotDueYet() {

        Subscription subscription =
                mock(Subscription.class);

        Instant evaluatedAt =
                Instant.parse(
                        "2026-10-23T20:00:00Z"
                );

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.CANCELLATION_SCHEDULED
                );

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(
                        Instant.parse(
                                "2026-11-23T20:00:00Z"
                        )
                );

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        SubscriptionLifecycleAction result =
                handler.handle(
                        new EvaluateSubscriptionLifecycleCommand(
                                UUID.randomUUID(),
                                evaluatedAt,
                                false
                        )
                );

        assertEquals(
                SubscriptionLifecycleAction.NONE,
                result
        );

        verifyNoInteractions(
                renewalHandler,
                expirationHandler
        );
    }

    @Test
    void shouldDoNothingForInactiveSubscription() {

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

        SubscriptionLifecycleAction result =
                handler.handle(
                        new EvaluateSubscriptionLifecycleCommand(
                                UUID.randomUUID(),
                                Instant.now(),
                                false
                        )
                );

        assertEquals(
                SubscriptionLifecycleAction.NONE,
                result
        );

        verifyNoInteractions(
                renewalHandler,
                expirationHandler
        );
    }
}