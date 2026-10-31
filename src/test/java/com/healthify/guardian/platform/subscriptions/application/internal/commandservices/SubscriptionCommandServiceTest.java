package com.healthify.guardian.platform.subscriptions.application.internal.commandservices;

import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EntitlementSetCommandService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.CancelSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ExpireSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCancellationCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RevokeAllEntitlementsCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionCommandServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private EntitlementSetCommandService entitlementSetCommandService;

    private SubscriptionCommandServiceImpl subscriptionCommandService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        subscriptionCommandService =
                new SubscriptionCommandServiceImpl(
                        subscriptionRepository,
                        planRepository,
                        entitlementSetCommandService
                );

        /*
         * Muy importante:
         * save() debe devolver exactamente la Subscription
         * que recibió y nunca null.
         */
        doAnswer(invocation ->
                invocation.getArgument(
                        0,
                        Subscription.class
                )
        ).when(subscriptionRepository)
                .save(any(Subscription.class));

        /*
         * Cancel y Expire revocan entitlements.
         */
        doReturn(
                Result.success(
                        new EntitlementSet()
                )
        ).when(entitlementSetCommandService)
                .handle(
                        any(RevokeAllEntitlementsCommand.class)
                );
    }

    @Test
    void shouldRequestSubscriptionCancellation() {

        UUID subscriptionId =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionId
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionId)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCancellationCommand(
                                subscriptionId
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        Subscription updated =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                SubscriptionStatus.CANCELLATION_SCHEDULED,
                updated.getStatus()
        );

        assertTrue(
                updated.isCancelAtPeriodEnd()
        );

        verify(subscriptionRepository)
                .save(subscription);

        /*
         * Una cancelación programada todavía conserva
         * sus beneficios.
         */
        verify(
                entitlementSetCommandService,
                never()
        ).handle(
                any(RevokeAllEntitlementsCommand.class)
        );
    }

    @Test
    void shouldCancelScheduledSubscription() {

        UUID subscriptionId =
                UUID.randomUUID();

        Subscription subscription =
                createScheduledCancellationSubscription(
                        subscriptionId
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionId)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new CancelSubscriptionCommand(
                                subscriptionId
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        Subscription updated =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                SubscriptionStatus.CANCELLED,
                updated.getStatus()
        );

        verify(entitlementSetCommandService)
                .handle(
                        any(RevokeAllEntitlementsCommand.class)
                );

        verify(subscriptionRepository)
                .save(subscription);
    }

    @Test
    void shouldExpireActiveSubscriptionWhenPeriodHasEnded() {

        UUID subscriptionId =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionId
                );

        subscription.setCurrentPeriodEnd(
                Instant.EPOCH
        );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionId)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new ExpireSubscriptionCommand(
                                subscriptionId
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        Subscription updated =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                SubscriptionStatus.EXPIRED,
                updated.getStatus()
        );

        verify(entitlementSetCommandService)
                .handle(
                        any(RevokeAllEntitlementsCommand.class)
                );

        verify(subscriptionRepository)
                .save(subscription);
    }

    @Test
    void shouldReturnFailureWhenSubscriptionDoesNotExist() {

        UUID subscriptionId =
                UUID.randomUUID();

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionId)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                subscriptionCommandService.handle(
                        new CancelSubscriptionCommand(
                                subscriptionId
                        )
                );

        assertTrue(
                result.isFailure()
        );

        assertTrue(
                result.toOptional().isEmpty()
        );

        verify(
                subscriptionRepository,
                never()
        ).save(
                any(Subscription.class)
        );

        verify(
                entitlementSetCommandService,
                never()
        ).handle(
                any(RevokeAllEntitlementsCommand.class)
        );
    }

    @Test
    void shouldReturnBusinessRuleFailureForInvalidTransition() {

        UUID subscriptionId =
                UUID.randomUUID();

        Subscription subscription =
                createPendingSubscription(
                        subscriptionId
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionId)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCancellationCommand(
                                subscriptionId
                        )
                );

        assertTrue(
                result.isFailure()
        );

        assertTrue(
                result.toOptional().isEmpty()
        );

        verify(
                subscriptionRepository,
                never()
        ).save(
                any(Subscription.class)
        );
    }

    private Subscription createActiveSubscription(
            UUID subscriptionId
    ) {

        Instant now =
                Instant.parse(
                        "2026-10-23T12:00:00Z"
                );

        Subscription subscription =
                baseSubscription(
                        subscriptionId
                );

        subscription.setStatus(
                SubscriptionStatus.ACTIVE
        );

        subscription.setCurrentPeriodStart(
                now
        );

        subscription.setCurrentPeriodEnd(
                Instant.parse(
                        "2026-11-23T12:00:00Z"
                )
        );

        subscription.setCancelAtPeriodEnd(
                false
        );

        subscription.setCreatedAt(
                now
        );

        subscription.setUpdatedAt(
                now
        );

        return subscription;
    }

    private Subscription createScheduledCancellationSubscription(
            UUID subscriptionId
    ) {

        Subscription subscription =
                createActiveSubscription(
                        subscriptionId
                );

        subscription.setStatus(
                SubscriptionStatus.CANCELLATION_SCHEDULED
        );

        subscription.setCancelAtPeriodEnd(
                true
        );

        return subscription;
    }

    private Subscription createPendingSubscription(
            UUID subscriptionId
    ) {

        Instant now =
                Instant.parse(
                        "2026-10-23T12:00:00Z"
                );

        Subscription subscription =
                baseSubscription(
                        subscriptionId
                );

        subscription.setStatus(
                SubscriptionStatus.PENDING
        );

        subscription.setCurrentPeriodStart(
                null
        );

        subscription.setCurrentPeriodEnd(
                null
        );

        subscription.setCancelAtPeriodEnd(
                false
        );

        subscription.setCreatedAt(
                now
        );

        subscription.setUpdatedAt(
                now
        );

        return subscription;
    }

    private Subscription baseSubscription(
            UUID subscriptionId
    ) {

        Subscription subscription =
                new Subscription();

        subscription.setId(
                new SubscriptionId(
                        subscriptionId
                )
        );

        subscription.setSubscriberId(
                new SubscriberId(
                        UUID.randomUUID()
                )
        );

        subscription.setPlanId(
                new PlanId(
                        UUID.randomUUID()
                )
        );

        return subscription;
    }
}