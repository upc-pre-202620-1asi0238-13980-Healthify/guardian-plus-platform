package com.healthify.guardian.platform.subscriptions.interfaces.rest;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.application.commandservices.SubscriptionCommandService;
import com.healthify.guardian.platform.subscriptions.application.queryservices.SubscriptionApplicationQueryService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ApplyPlanChangeCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCancellationCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetAvailableEntitlementsQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetSubscriptionStatusQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.ChangeSubscriptionPlanResource;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.RequestSubscriptionResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionControllerTest {

    @Mock
    private SubscriptionCommandService subscriptionCommandService;

    @Mock
    private SubscriptionApplicationQueryService subscriptionQueryService;

    private SubscriptionController controller;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        controller =
                new SubscriptionController(
                        subscriptionCommandService,
                        subscriptionQueryService
                );
    }

    @Test
    void shouldReturnCreatedWhenSubscriptionIsRequested() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID subscriberUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        planUuid,
                        SubscriptionStatus.PENDING
                );

        doReturn(
                Result.success(subscription)
        ).when(subscriptionCommandService)
                .handle(
                        any(RequestSubscriptionCommand.class)
                );

        var response =
                controller.requestSubscription(
                        new RequestSubscriptionResource(
                                subscriberUuid,
                                planUuid
                        )
                );

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                subscriptionUuid,
                response.getBody().id()
        );

        assertEquals(
                subscriberUuid,
                response.getBody().subscriberId()
        );

        assertEquals(
                planUuid,
                response.getBody().planId()
        );

        assertEquals(
                "PENDING",
                response.getBody().status()
        );
    }

    @Test
    void shouldReturnBadRequestWhenSubscriptionRequestFails() {

        doReturn(
                Result.failure(
                        ApplicationError.businessRuleViolation(
                                "request-subscription",
                                "Subscriber already has a subscription"
                        )
                )
        ).when(subscriptionCommandService)
                .handle(
                        any(RequestSubscriptionCommand.class)
                );

        var response =
                controller.requestSubscription(
                        new RequestSubscriptionResource(
                                UUID.randomUUID(),
                                UUID.randomUUID()
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNull(
                response.getBody()
        );
    }

    @Test
    void shouldReturnOkWhenPlanIsChanged() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID subscriberUuid = UUID.randomUUID();
        UUID newPlanUuid = UUID.randomUUID();

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        newPlanUuid,
                        SubscriptionStatus.ACTIVE
                );

        doReturn(
                Result.success(subscription)
        ).when(subscriptionCommandService)
                .handle(
                        any(ApplyPlanChangeCommand.class)
                );

        var response =
                controller.changePlan(
                        subscriptionUuid,
                        new ChangeSubscriptionPlanResource(
                                newPlanUuid
                        )
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                newPlanUuid,
                response.getBody().planId()
        );

        assertEquals(
                "ACTIVE",
                response.getBody().status()
        );
    }

    @Test
    void shouldReturnBadRequestWhenPlanChangeFails() {

        doReturn(
                Result.failure(
                        ApplicationError.businessRuleViolation(
                                "apply-plan-change",
                                "Unable to change subscription plan"
                        )
                )
        ).when(subscriptionCommandService)
                .handle(
                        any(ApplyPlanChangeCommand.class)
                );

        var response =
                controller.changePlan(
                        UUID.randomUUID(),
                        new ChangeSubscriptionPlanResource(
                                UUID.randomUUID()
                        )
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNull(
                response.getBody()
        );
    }

    @Test
    void shouldReturnOkWhenCancellationIsRequested() {

        UUID subscriptionUuid = UUID.randomUUID();

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        SubscriptionStatus.CANCELLATION_SCHEDULED
                );

        subscription.setCancelAtPeriodEnd(
                true
        );

        doReturn(
                Result.success(subscription)
        ).when(subscriptionCommandService)
                .handle(
                        any(RequestSubscriptionCancellationCommand.class)
                );

        var response =
                controller.requestCancellation(
                        subscriptionUuid
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                "CANCELLATION_SCHEDULED",
                response.getBody().status()
        );

        assertTrue(
                response.getBody().cancelAtPeriodEnd()
        );
    }

    @Test
    void shouldReturnBadRequestWhenCancellationRequestFails() {

        doReturn(
                Result.failure(
                        ApplicationError.businessRuleViolation(
                                "request-subscription-cancellation",
                                "Unable to request cancellation"
                        )
                )
        ).when(subscriptionCommandService)
                .handle(
                        any(RequestSubscriptionCancellationCommand.class)
                );

        var response =
                controller.requestCancellation(
                        UUID.randomUUID()
                );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNull(
                response.getBody()
        );
    }

    @Test
    void shouldReturnCurrentSubscriptionStatus() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID subscriberUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        planUuid,
                        SubscriptionStatus.ACTIVE
                );

        doReturn(
                Result.success(subscription)
        ).when(subscriptionQueryService)
                .handle(
                        any(GetSubscriptionStatusQuery.class)
                );

        var response =
                controller.getSubscriptionStatus(
                        subscriberUuid
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                subscriptionUuid,
                response.getBody().id()
        );

        assertEquals(
                "ACTIVE",
                response.getBody().status()
        );
    }

    @Test
    void shouldReturnNotFoundWhenSubscriberHasNoCurrentSubscription() {

        doReturn(
                Result.failure(
                        ApplicationError.notFound(
                                "Subscription",
                                UUID.randomUUID().toString()
                        )
                )
        ).when(subscriptionQueryService)
                .handle(
                        any(GetSubscriptionStatusQuery.class)
                );

        var response =
                controller.getSubscriptionStatus(
                        UUID.randomUUID()
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNull(
                response.getBody()
        );
    }

    @Test
    void shouldReturnAvailableEntitlements() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        EntitlementSet entitlementSet =
                createEntitlementSet(
                        subscriptionUuid
                );

        doReturn(
                Result.success(entitlementSet)
        ).when(subscriptionQueryService)
                .handle(
                        any(GetAvailableEntitlementsQuery.class)
                );

        var response =
                controller.getAvailableEntitlements(
                        UUID.randomUUID()
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertNotNull(
                response.getBody()
        );

        assertEquals(
                subscriptionUuid,
                response.getBody()
                        .subscriptionId()
        );

        assertEquals(
                2,
                response.getBody()
                        .entitlements()
                        .size()
        );

        assertTrue(
                response.getBody()
                        .entitlements()
                        .stream()
                        .anyMatch(
                                entitlement ->
                                        entitlement.code()
                                                .equals(
                                                        "REAL_TIME_LOCATION"
                                                )
                        )
        );

        assertTrue(
                response.getBody()
                        .entitlements()
                        .stream()
                        .anyMatch(
                                entitlement ->
                                        entitlement.code()
                                                .equals(
                                                        "HEALTH_MONITORING"
                                                )
                        )
        );
    }

    @Test
    void shouldReturnNotFoundWhenEntitlementsAreUnavailable() {

        doReturn(
                Result.failure(
                        ApplicationError.notFound(
                                "EntitlementSet",
                                UUID.randomUUID().toString()
                        )
                )
        ).when(subscriptionQueryService)
                .handle(
                        any(GetAvailableEntitlementsQuery.class)
                );

        var response =
                controller.getAvailableEntitlements(
                        UUID.randomUUID()
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNull(
                response.getBody()
        );
    }

    private Subscription createSubscription(
            UUID subscriptionUuid,
            UUID subscriberUuid,
            UUID planUuid,
            SubscriptionStatus status
    ) {

        Instant now =
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
                        planUuid
                )
        );

        subscription.setStatus(
                status
        );

        if (status == SubscriptionStatus.PENDING) {

            subscription.setCurrentPeriodStart(
                    null
            );

            subscription.setCurrentPeriodEnd(
                    null
            );

        } else {

            subscription.setCurrentPeriodStart(
                    now
            );

            subscription.setCurrentPeriodEnd(
                    Instant.parse(
                            "2026-11-23T10:00:00Z"
                    )
            );
        }

        subscription.setCancelAtPeriodEnd(
                status ==
                        SubscriptionStatus.CANCELLATION_SCHEDULED
        );

        subscription.setCreatedAt(
                now
        );

        subscription.setUpdatedAt(
                now
        );

        return subscription;
    }

    private EntitlementSet createEntitlementSet(
            UUID subscriptionUuid
    ) {

        EntitlementSet entitlementSet =
                new EntitlementSet();

        entitlementSet.setSubscriptionId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        entitlementSet.setEntitlements(
                Set.of(
                        createEntitlement(
                                "REAL_TIME_LOCATION",
                                "Real-time location"
                        ),
                        createEntitlement(
                                "HEALTH_MONITORING",
                                "Health monitoring"
                        )
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