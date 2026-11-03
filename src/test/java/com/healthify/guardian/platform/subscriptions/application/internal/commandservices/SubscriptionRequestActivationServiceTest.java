package com.healthify.guardian.platform.subscriptions.application.internal.commandservices;

import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EntitlementSetCommandService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ActivateSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCommand;
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

class SubscriptionRequestActivationServiceTest {

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
    }

    @Test
    void shouldRequestSubscriptionAndCreatePendingSubscription() {

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Plan plan =
                createPlan(
                        planUuid,
                        true
                );

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(plan)
        );

        when(subscriptionRepository.findActiveBySubscriberId(
                new SubscriberId(subscriberUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCommand(
                                subscriberUuid,
                                planUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        Subscription created =
                result.toOptional()
                        .orElseThrow();

        assertNotNull(
                created.getId()
        );

        assertEquals(
                new SubscriberId(subscriberUuid),
                created.getSubscriberId()
        );

        assertEquals(
                new PlanId(planUuid),
                created.getPlanId()
        );

        assertEquals(
                SubscriptionStatus.PENDING,
                created.getStatus()
        );

        assertNull(
                created.getCurrentPeriodStart()
        );

        assertNull(
                created.getCurrentPeriodEnd()
        );

        assertFalse(
                created.isCancelAtPeriodEnd()
        );

        assertNotNull(
                created.getCreatedAt()
        );

        verify(subscriptionRepository)
                .save(
                        any(Subscription.class)
                );

        /*
         * PENDING todavía no recibe beneficios.
         */
        verify(
                entitlementSetCommandService,
                never()
        ).handle(
                any(SyncEntitlementsFromPlanCommand.class)
        );
    }

    @Test
    void shouldFailRequestWhenPlanDoesNotExist() {

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCommand(
                                subscriberUuid,
                                planUuid
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
    void shouldFailRequestWhenPlanIsInactive() {

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Plan inactivePlan =
                createPlan(
                        planUuid,
                        false
                );

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(inactivePlan)
        );

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCommand(
                                subscriberUuid,
                                planUuid
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
    void shouldFailRequestWhenSubscriberAlreadyHasActiveSubscription() {

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Plan plan =
                createPlan(
                        planUuid,
                        true
                );

        Subscription existing =
                createActiveSubscription(
                        UUID.randomUUID(),
                        subscriberUuid,
                        planUuid
                );

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(plan)
        );

        when(subscriptionRepository.findActiveBySubscriberId(
                new SubscriberId(subscriberUuid)
        )).thenReturn(
                Optional.of(existing)
        );

        var result =
                subscriptionCommandService.handle(
                        new RequestSubscriptionCommand(
                                subscriberUuid,
                                planUuid
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
    void shouldActivatePendingSubscription() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Plan plan =
                createPlan(
                        planUuid,
                        true
                );

        Subscription pending =
                new Subscription(
                        new SubscriberId(
                                subscriberUuid
                        ),
                        new PlanId(
                                planUuid
                        ),
                        Instant.parse(
                                "2026-10-23T12:00:00Z"
                        )
                );

        pending.setId(
                new SubscriptionId(
                        subscriptionUuid
                )
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

        Instant periodStart =
                Instant.parse(
                        "2026-10-23T13:00:00Z"
                );

        Instant periodEnd =
                Instant.parse(
                        "2026-11-23T13:00:00Z"
                );

        var result =
                subscriptionCommandService.handle(
                        new ActivateSubscriptionCommand(
                                subscriptionUuid,
                                planUuid,
                                periodStart,
                                periodEnd
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        Subscription activated =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                SubscriptionStatus.ACTIVE,
                activated.getStatus()
        );

        assertEquals(
                new PlanId(planUuid),
                activated.getPlanId()
        );

        assertEquals(
                periodStart,
                activated.getCurrentPeriodStart()
        );

        assertEquals(
                periodEnd,
                activated.getCurrentPeriodEnd()
        );

        assertFalse(
                activated.isCancelAtPeriodEnd()
        );

        assertNotNull(
                activated.getUpdatedAt()
        );

        verify(entitlementSetCommandService)
                .handle(
                        any(SyncEntitlementsFromPlanCommand.class)
                );

        verify(subscriptionRepository)
                .save(pending);
    }

    @Test
    void shouldFailWhenTryingToActivateAlreadyActiveSubscription() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Plan plan =
                createPlan(
                        planUuid,
                        true
                );

        Subscription active =
                createActiveSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        planUuid
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(active)
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
                                        "2026-10-23T14:00:00Z"
                                ),
                                Instant.parse(
                                        "2026-11-23T14:00:00Z"
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

        verify(
                entitlementSetCommandService,
                never()
        ).handle(
                any(SyncEntitlementsFromPlanCommand.class)
        );
    }

    private Plan createPlan(
            UUID planUuid,
            boolean active
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
                active
        );

        plan.setEntitlementCodes(
                Set.of(
                        new EntitlementCode(
                                "REAL_TIME_LOCATION"
                        )
                )
        );

        return plan;
    }

    private Subscription createActiveSubscription(
            UUID subscriptionUuid,
            UUID subscriberUuid,
            UUID planUuid
    ) {

        Instant start =
                Instant.parse(
                        "2026-10-23T12:00:00Z"
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
                SubscriptionStatus.ACTIVE
        );

        subscription.setCurrentPeriodStart(
                start
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
                start
        );

        subscription.setUpdatedAt(
                start
        );

        return subscription;
    }
}