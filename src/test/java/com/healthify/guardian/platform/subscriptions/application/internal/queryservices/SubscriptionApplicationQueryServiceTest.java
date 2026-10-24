package com.healthify.guardian.platform.subscriptions.application.internal.queryservices;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetAvailableEntitlementsQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetSubscriptionStatusQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementSetRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionApplicationQueryServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private EntitlementSetRepository entitlementSetRepository;

    private SubscriptionApplicationQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        queryService =
                new SubscriptionApplicationQueryServiceImpl(
                        subscriptionRepository,
                        entitlementSetRepository
                );
    }

    @Test
    void shouldReturnActiveSubscriptionStatus() {

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        SubscriptionStatus.ACTIVE
                );

        when(subscriptionRepository.findActiveBySubscriberId(
                new SubscriberId(subscriberUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                queryService.handle(
                        new GetSubscriptionStatusQuery(
                                subscriberUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        Subscription found =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                new SubscriptionId(subscriptionUuid),
                found.getId()
        );

        assertEquals(
                SubscriptionStatus.ACTIVE,
                found.getStatus()
        );
    }

    @Test
    void shouldReturnScheduledCancellationAsCurrentSubscription() {

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        SubscriptionStatus.CANCELLATION_SCHEDULED
                );

        subscription.setCancelAtPeriodEnd(
                true
        );

        when(subscriptionRepository.findActiveBySubscriberId(
                new SubscriberId(subscriberUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                queryService.handle(
                        new GetSubscriptionStatusQuery(
                                subscriberUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        Subscription found =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                SubscriptionStatus.CANCELLATION_SCHEDULED,
                found.getStatus()
        );

        assertTrue(
                found.isCancelAtPeriodEnd()
        );
    }

    @Test
    void shouldFailWhenSubscriberHasNoCurrentSubscription() {

        UUID subscriberUuid =
                UUID.randomUUID();

        when(subscriptionRepository.findActiveBySubscriberId(
                new SubscriberId(subscriberUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                queryService.handle(
                        new GetSubscriptionStatusQuery(
                                subscriberUuid
                        )
                );

        assertTrue(
                result.isFailure()
        );

        assertTrue(
                result.toOptional().isEmpty()
        );
    }

    @Test
    void shouldReturnAvailableEntitlements() {

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        SubscriptionStatus.ACTIVE
                );

        EntitlementSet entitlementSet =
                createEntitlementSet(
                        subscriptionUuid
                );

        when(subscriptionRepository.findActiveBySubscriberId(
                new SubscriberId(subscriberUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(entitlementSet)
        );

        var result =
                queryService.handle(
                        new GetAvailableEntitlementsQuery(
                                subscriberUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        EntitlementSet found =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                new SubscriptionId(subscriptionUuid),
                found.getSubscriptionId()
        );

        assertEquals(
                2,
                found.getEntitlements().size()
        );

        assertTrue(
                found.contains(
                        new EntitlementCode(
                                "REAL_TIME_LOCATION"
                        )
                )
        );

        assertTrue(
                found.contains(
                        new EntitlementCode(
                                "HEALTH_MONITORING"
                        )
                )
        );
    }

    @Test
    void shouldFailWhenEntitlementSetDoesNotExist() {

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        SubscriptionStatus.ACTIVE
                );

        when(subscriptionRepository.findActiveBySubscriberId(
                new SubscriberId(subscriberUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                queryService.handle(
                        new GetAvailableEntitlementsQuery(
                                subscriberUuid
                        )
                );

        assertTrue(
                result.isFailure()
        );

        assertTrue(
                result.toOptional().isEmpty()
        );
    }

    @Test
    void shouldFailEntitlementQueryWhenSubscriberHasNoCurrentSubscription() {

        UUID subscriberUuid =
                UUID.randomUUID();

        when(subscriptionRepository.findActiveBySubscriberId(
                new SubscriberId(subscriberUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                queryService.handle(
                        new GetAvailableEntitlementsQuery(
                                subscriberUuid
                        )
                );

        assertTrue(
                result.isFailure()
        );

        assertTrue(
                result.toOptional().isEmpty()
        );

        verify(
                entitlementSetRepository,
                never()
        ).findBySubscriptionId(
                any(SubscriptionId.class)
        );
    }

    private Subscription createSubscription(
            UUID subscriptionUuid,
            UUID subscriberUuid,
            SubscriptionStatus status
    ) {

        Instant start =
                Instant.parse(
                        "2026-10-23T10:00:00Z"
                );

        Subscription subscription =
                new Subscription();

        subscription.setId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        subscription.setSubscriberId(
                new SubscriberId(
                        subscriberUuid
                )
        );

        subscription.setPlanId(
                new PlanId(
                        UUID.randomUUID()
                )
        );

        subscription.setStatus(
                status
        );

        subscription.setCurrentPeriodStart(
                start
        );

        subscription.setCurrentPeriodEnd(
                Instant.parse(
                        "2026-11-23T10:00:00Z"
                )
        );

        subscription.setCancelAtPeriodEnd(
                status ==
                        SubscriptionStatus.CANCELLATION_SCHEDULED
        );

        subscription.setCreatedAt(
                start
        );

        subscription.setUpdatedAt(
                start
        );

        return subscription;
    }

    private EntitlementSet createEntitlementSet(
            UUID subscriptionUuid
    ) {

        Entitlement location =
                createEntitlement(
                        "REAL_TIME_LOCATION",
                        "Real-time location"
                );

        Entitlement health =
                createEntitlement(
                        "HEALTH_MONITORING",
                        "Health monitoring"
                );

        EntitlementSet entitlementSet =
                new EntitlementSet();

        entitlementSet.setSubscriptionId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        entitlementSet.setEntitlements(
                Set.of(
                        location,
                        health
                )
        );

        entitlementSet.setUpdatedAt(
                Instant.parse(
                        "2026-10-23T10:00:00Z"
                )
        );

        return entitlementSet;
    }

    private Entitlement createEntitlement(
            String code,
            String name
    ) {

        Entitlement entitlement =
                new Entitlement();

        entitlement.setCode(
                new EntitlementCode(
                        code
                )
        );

        entitlement.setName(
                name
        );

        entitlement.setDescription(
                name + " capability"
        );

        return entitlement;
    }
}