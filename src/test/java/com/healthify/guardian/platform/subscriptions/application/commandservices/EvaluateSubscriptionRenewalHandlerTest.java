package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionRenewalCommand;
import com.healthify.guardian.platform.subscriptions.application.commandservices.impl.EvaluateSubscriptionRenewalHandlerImpl;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.RenewalEvaluation;
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

class EvaluateSubscriptionRenewalHandlerTest {

    private SubscriptionRepository subscriptionRepository;

    private EvaluateSubscriptionRenewalHandler handler;

    @BeforeEach
    void setUp() {

        subscriptionRepository =
                mock(SubscriptionRepository.class);

        handler =
                new EvaluateSubscriptionRenewalHandlerImpl(
                        subscriptionRepository
                );
    }

    @Test
    void shouldMarkActiveExpiredPeriodAsDueForRenewal() {

        Subscription subscription =
                mock(Subscription.class);

        Instant evaluatedAt =
                Instant.parse("2026-10-23T20:00:00Z");

        when(subscription.getStatus())
                .thenReturn(SubscriptionStatus.ACTIVE);

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(false);

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

        RenewalEvaluation result =
                handler.handle(
                        new EvaluateSubscriptionRenewalCommand(
                                UUID.randomUUID(),
                                evaluatedAt
                        )
                );

        assertEquals(
                RenewalEvaluation.DUE,
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

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(false);

        when(subscription.getCurrentPeriodEnd())
                .thenReturn(
                        Instant.parse(
                                "2026-10-24T20:00:00Z"
                        )
                );

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        RenewalEvaluation result =
                handler.handle(
                        new EvaluateSubscriptionRenewalCommand(
                                UUID.randomUUID(),
                                evaluatedAt
                        )
                );

        assertEquals(
                RenewalEvaluation.NOT_DUE,
                result
        );
    }

    @Test
    void shouldRejectRenewalWhenCancellationIsScheduled() {

        Subscription subscription =
                mock(Subscription.class);

        when(subscription.getStatus())
                .thenReturn(SubscriptionStatus.ACTIVE);

        when(subscription.isCancelAtPeriodEnd())
                .thenReturn(true);

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        RenewalEvaluation result =
                handler.handle(
                        new EvaluateSubscriptionRenewalCommand(
                                UUID.randomUUID(),
                                Instant.now()
                        )
                );

        assertEquals(
                RenewalEvaluation.NOT_ELIGIBLE,
                result
        );
    }

    @Test
    void shouldRejectNonActiveSubscription() {

        Subscription subscription =
                mock(Subscription.class);

        when(subscription.getStatus())
                .thenReturn(SubscriptionStatus.EXPIRED);

        when(
                subscriptionRepository.findById(
                        any(SubscriptionId.class)
                )
        ).thenReturn(Optional.of(subscription));

        RenewalEvaluation result =
                handler.handle(
                        new EvaluateSubscriptionRenewalCommand(
                                UUID.randomUUID(),
                                Instant.now()
                        )
                );

        assertEquals(
                RenewalEvaluation.NOT_ELIGIBLE,
                result
        );
    }
}