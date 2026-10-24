package com.healthify.guardian.platform.subscriptions.application.internal.commandservices;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EntitlementSetCommandService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ActivateSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ApplyPlanChangeCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.CancelSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ExpireSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCancellationCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RevokeAllEntitlementsCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.SyncEntitlementsFromPlanCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.EntitlementCode;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.Money;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubscriptionEntitlementLifecycleOrchestrationTest {

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

        doAnswer(invocation ->
                invocation.getArgument(
                        0,
                        Subscription.class
                )
        ).when(subscriptionRepository)
                .save(any(Subscription.class));

        doReturn(
                Result.success(
                        new EntitlementSet()
                )
        ).when(entitlementSetCommandService)
                .handle(
                        any(SyncEntitlementsFromPlanCommand.class)
                );

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
    void shouldSynchronizePlanEntitlementsWhenSubscriptionIsActivated() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Subscription pending =
                new Subscription(
                        new SubscriberId(
                                subscriberUuid
                        ),
                        new PlanId(
                                planUuid
                        ),
                        Instant.parse(
                                "2026-10-23T10:00:00Z"
                        )
                );

        pending.setId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        Plan plan =
                createPlan(
                        planUuid
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(pending)
        );

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(plan)
        );

        var result =
                subscriptionCommandService.handle(
                        new ActivateSubscriptionCommand(
                                subscriptionUuid,
                                planUuid,
                                Instant.parse(
                                        "2026-10-23T12:00:00Z"
                                ),
                                Instant.parse(
                                        "2026-11-23T12:00:00Z"
                                )
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        verify(entitlementSetCommandService)
                .handle(
                        eq(
                                new SyncEntitlementsFromPlanCommand(
                                        subscriptionUuid,
                                        planUuid
                                )
                        )
                );

        verify(subscriptionRepository)
                .save(pending);

        assertEquals(
                SubscriptionStatus.ACTIVE,
                pending.getStatus()
        );
    }

    @Test
    void shouldSynchronizeNewPlanEntitlementsWhenPlanChanges() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID currentPlanUuid =
                UUID.randomUUID();

        UUID newPlanUuid =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        currentPlanUuid
                );

        Plan newPlan =
                createPlan(
                        newPlanUuid
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        when(planRepository.findById(
                new PlanId(newPlanUuid)
        )).thenReturn(
                Optional.of(newPlan)
        );

        var result =
                subscriptionCommandService.handle(
                        new ApplyPlanChangeCommand(
                                subscriptionUuid,
                                newPlanUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        verify(entitlementSetCommandService)
                .handle(
                        eq(
                                new SyncEntitlementsFromPlanCommand(
                                        subscriptionUuid,
                                        newPlanUuid
                                )
                        )
                );

        verify(subscriptionRepository)
                .save(subscription);

        assertEquals(
                new PlanId(newPlanUuid),
                subscription.getPlanId()
        );
    }

    @Test
    void shouldKeepEntitlementsWhenCancellationIsOnlyRequested() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        UUID.randomUUID()
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCancellationCommand(
                                subscriptionUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        assertEquals(
                SubscriptionStatus.CANCELLATION_SCHEDULED,
                subscription.getStatus()
        );

        assertTrue(
                subscription.isCancelAtPeriodEnd()
        );

        verify(
                entitlementSetCommandService,
                never()
        ).handle(
                any(RevokeAllEntitlementsCommand.class)
        );

        verify(subscriptionRepository)
                .save(subscription);
    }

    @Test
    void shouldRevokeAllEntitlementsWhenSubscriptionIsCancelled() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createScheduledCancellationSubscription(
                        subscriptionUuid,
                        UUID.randomUUID()
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new CancelSubscriptionCommand(
                                subscriptionUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        verify(entitlementSetCommandService)
                .handle(
                        eq(
                                new RevokeAllEntitlementsCommand(
                                        subscriptionUuid
                                )
                        )
                );

        verify(subscriptionRepository)
                .save(subscription);

        assertEquals(
                SubscriptionStatus.CANCELLED,
                subscription.getStatus()
        );
    }

    @Test
    void shouldRevokeAllEntitlementsWhenSubscriptionExpires() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        UUID.randomUUID()
                );

        subscription.setCurrentPeriodEnd(
                Instant.EPOCH
        );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new ExpireSubscriptionCommand(
                                subscriptionUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        verify(entitlementSetCommandService)
                .handle(
                        eq(
                                new RevokeAllEntitlementsCommand(
                                        subscriptionUuid
                                )
                        )
                );

        verify(subscriptionRepository)
                .save(subscription);

        assertEquals(
                SubscriptionStatus.EXPIRED,
                subscription.getStatus()
        );
    }

    @Test
    void shouldNotSaveActivatedSubscriptionWhenEntitlementSynchronizationFails() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Subscription pending =
                new Subscription(
                        new SubscriberId(
                                subscriberUuid
                        ),
                        new PlanId(
                                planUuid
                        ),
                        Instant.parse(
                                "2026-10-23T10:00:00Z"
                        )
                );

        pending.setId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        Plan plan =
                createPlan(
                        planUuid
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(pending)
        );

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(plan)
        );

        doReturn(
                Result.failure(
                        ApplicationError.businessRuleViolation(
                                "sync-entitlements-from-plan",
                                "Unable to synchronize entitlements"
                        )
                )
        ).when(entitlementSetCommandService)
                .handle(
                        any(SyncEntitlementsFromPlanCommand.class)
                );

        var result =
                subscriptionCommandService.handle(
                        new ActivateSubscriptionCommand(
                                subscriptionUuid,
                                planUuid,
                                Instant.parse(
                                        "2026-10-23T12:00:00Z"
                                ),
                                Instant.parse(
                                        "2026-11-23T12:00:00Z"
                                )
                        )
                );

        assertTrue(
                result.isFailure()
        );

        verify(
                subscriptionRepository,
                never()
        ).save(
                any(Subscription.class)
        );
    }

    @Test
    void shouldNotSaveCancelledSubscriptionWhenEntitlementRevocationFails() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createScheduledCancellationSubscription(
                        subscriptionUuid,
                        UUID.randomUUID()
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        doReturn(
                Result.failure(
                        ApplicationError.businessRuleViolation(
                                "revoke-all-entitlements",
                                "Unable to revoke entitlements"
                        )
                )
        ).when(entitlementSetCommandService)
                .handle(
                        any(RevokeAllEntitlementsCommand.class)
                );

        var result =
                subscriptionCommandService.handle(
                        new CancelSubscriptionCommand(
                                subscriptionUuid
                        )
                );

        assertTrue(
                result.isFailure()
        );

        verify(
                subscriptionRepository,
                never()
        ).save(
                any(Subscription.class)
        );
    }

    private Plan createPlan(
            UUID planUuid
    ) {

        Plan plan =
                new Plan();

        plan.setId(
                new PlanId(
                        planUuid
                )
        );

        plan.setName(
                "Guardian Plus Premium"
        );

        plan.setPrice(
                new Money(
                        new BigDecimal("29.90"),
                        Currency.getInstance("PEN")
                )
        );

        plan.setBillingCycle(
                BillingCycle.MONTHLY
        );

        plan.setActive(
                true
        );

        plan.setEntitlementCodes(
                Set.of(
                        new EntitlementCode(
                                "REAL_TIME_LOCATION"
                        ),
                        new EntitlementCode(
                                "HEALTH_MONITORING"
                        )
                )
        );

        return plan;
    }

    private Subscription createActiveSubscription(
            UUID subscriptionUuid,
            UUID planUuid
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
                        UUID.randomUUID()
                )
        );

        subscription.setPlanId(
                new PlanId(
                        planUuid
                )
        );

        subscription.setStatus(
                SubscriptionStatus.ACTIVE
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
                false
        );

        subscription.setCreatedAt(
                start
        );

        subscription.setUpdatedAt(
                start
        );

        return subscription;
    }

    private Subscription createScheduledCancellationSubscription(
            UUID subscriptionUuid,
            UUID planUuid
    ) {

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        planUuid
                );

        subscription.setStatus(
                SubscriptionStatus.CANCELLATION_SCHEDULED
        );

        subscription.setCancelAtPeriodEnd(
                true
        );

        return subscription;
    }
}