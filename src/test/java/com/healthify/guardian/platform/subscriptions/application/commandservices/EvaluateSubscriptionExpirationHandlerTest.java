package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionExpirationCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.EvaluateSubscriptionExpirationHandlerImpl;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.ExpirationEvaluation;
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

class EvaluateSubscriptionExpirationHandlerTest {

    private SubscriptionRepository subscriptionRepository;

    private EvaluateSubscriptionExpirationHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        handler =
                new EvaluateSubscriptionExpirationHandlerImpl(
                        subscriptionRepository
                );
    }

    @Test
    void shouldMarkActiveExpiredPeriodAsDue() {

        Subscription subscription =
                mock(Subscription.class);

        Instant evaluatedAt =
                Instant.parse("2026-10-23T20:00:00Z");

        when(subscription.getStatus())
                .thenReturn(SubscriptionStatus.ACTIVE);

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

        ExpirationEvaluation result =
                handler.handle(
                        new EvaluateSubscriptionExpirationCommand(
                                UUID.randomUUID(),
                                evaluatedAt
                        )
                );

        assertEquals(
                ExpirationEvaluation.DUE,
                result
        );
    }

    @Test
    void shouldMarkFuturePeriodAsNotDue() {

        Subscription subscription =
                mock(Subscription.class);

        Instant evaluatedAt =
                Instant.parse("2026-10-23T20:00:00Z");

        when(subscription.getStatus())
                .thenReturn(SubscriptionStatus.ACTIVE);

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

        ExpirationEvaluation result =
                handler.handle(
                        new EvaluateSubscriptionExpirationCommand(
                                UUID.randomUUID(),
                                evaluatedAt
                        )
                );

        assertEquals(
                ExpirationEvaluation.NOT_DUE,
                result
        );
    }

    @Test
    void shouldNotExpireCancellationScheduledSubscription() {

        Subscription subscription =
                mock(Subscription.class);

        when(subscription.getStatus())
                .thenReturn(
                        SubscriptionStatus.CANCELLATION_SCHEDULED
                );

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        ExpirationEvaluation result =
                handler.handle(
                        new EvaluateSubscriptionExpirationCommand(
                                UUID.randomUUID(),
                                Instant.now()
                        )
                );

        assertEquals(
                ExpirationEvaluation.NOT_ELIGIBLE,
                result
        );
    }

    @Test
    void shouldNotExpireAlreadyExpiredSubscription() {

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

        ExpirationEvaluation result =
                handler.handle(
                        new EvaluateSubscriptionExpirationCommand(
                                UUID.randomUUID(),
                                Instant.now()
                        )
                );

        assertEquals(
                ExpirationEvaluation.NOT_ELIGIBLE,
                result
        );
    }
}