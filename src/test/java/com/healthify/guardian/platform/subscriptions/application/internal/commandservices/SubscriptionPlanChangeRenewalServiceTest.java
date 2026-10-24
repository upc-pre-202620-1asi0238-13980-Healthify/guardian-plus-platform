package com.healthify.guardian.platform.subscriptions.application.internal.commandservices;

import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.application.commandservices.EntitlementSetCommandService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ApplyPlanChangeCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RenewSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.SyncEntitlementsFromPlanCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.*;
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

class SubscriptionPlanChangeRenewalServiceTest {

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
    void shouldChangePlanForActiveSubscription() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID currentPlanUuid =
                UUID.randomUUID();

        UUID newPlanUuid =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        currentPlanUuid
                );

        Plan newPlan =
                createPlan(
                        newPlanUuid,
                        true
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

        Subscription updated =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                new PlanId(newPlanUuid),
                updated.getPlanId()
        );

        assertEquals(
                SubscriptionStatus.ACTIVE,
                updated.getStatus()
        );

        assertNotNull(
                updated.getUpdatedAt()
        );

        verify(entitlementSetCommandService)
                .handle(
                        any(SyncEntitlementsFromPlanCommand.class)
                );

        verify(subscriptionRepository)
                .save(subscription);
    }

    @Test
    void shouldFailPlanChangeWhenNewPlanDoesNotExist() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID newPlanUuid =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        when(planRepository.findById(
                new PlanId(newPlanUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                subscriptionCommandService.handle(
                        new ApplyPlanChangeCommand(
                                subscriptionUuid,
                                newPlanUuid
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
    void shouldFailPlanChangeWhenNewPlanIsInactive() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID newPlanUuid =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        Plan inactivePlan =
                createPlan(
                        newPlanUuid,
                        false
                );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        when(planRepository.findById(
                new PlanId(newPlanUuid)
        )).thenReturn(
                Optional.of(inactivePlan)
        );

        var result =
                subscriptionCommandService.handle(
                        new ApplyPlanChangeCommand(
                                subscriptionUuid,
                                newPlanUuid
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
    void shouldFailPlanChangeWhenSubscriptionIsNotActive() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID newPlanUuid =
                UUID.randomUUID();

        Subscription subscription =
                createPendingSubscription(
                        subscriptionUuid
                );

        Plan newPlan =
                createPlan(
                        newPlanUuid,
                        true
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
    void shouldRenewActiveSubscriptionAndMovePeriodForward() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        Instant oldPeriodEnd =
                Instant.parse(
                        "2026-11-23T12:00:00Z"
                );

        Instant newPeriodEnd =
                Instant.parse(
                        "2026-12-23T12:00:00Z"
                );

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        subscription.setCurrentPeriodEnd(
                oldPeriodEnd
        );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new RenewSubscriptionCommand(
                                subscriptionUuid,
                                newPeriodEnd
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        Subscription renewed =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                oldPeriodEnd,
                renewed.getCurrentPeriodStart()
        );

        assertEquals(
                newPeriodEnd,
                renewed.getCurrentPeriodEnd()
        );

        assertEquals(
                SubscriptionStatus.ACTIVE,
                renewed.getStatus()
        );

        assertFalse(
                renewed.isCancelAtPeriodEnd()
        );

        verify(subscriptionRepository)
                .save(subscription);

        /*
         * Renovar el mismo plan no necesita cambiar
         * el EntitlementSet.
         */
        verify(
                entitlementSetCommandService,
                never()
        ).handle(
                any(SyncEntitlementsFromPlanCommand.class)
        );
    }

    @Test
    void shouldFailRenewalWhenCancellationIsScheduled() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        subscription.setStatus(
                SubscriptionStatus.CANCELLATION_SCHEDULED
        );

        subscription.setCancelAtPeriodEnd(
                true
        );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new RenewSubscriptionCommand(
                                subscriptionUuid,
                                Instant.parse(
                                        "2026-12-23T12:00:00Z"
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
    void shouldFailRenewalWhenNewPeriodEndIsNotAfterCurrentPeriodEnd() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        Instant currentPeriodEnd =
                Instant.parse(
                        "2026-11-23T12:00:00Z"
                );

        Subscription subscription =
                createActiveSubscription(
                        subscriptionUuid,
                        UUID.randomUUID(),
                        UUID.randomUUID()
                );

        subscription.setCurrentPeriodEnd(
                currentPeriodEnd
        );

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(subscription)
        );

        var result =
                subscriptionCommandService.handle(
                        new RenewSubscriptionCommand(
                                subscriptionUuid,
                                currentPeriodEnd
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
    void shouldFailRenewalWhenSubscriptionDoesNotExist() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        when(subscriptionRepository.findById(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                subscriptionCommandService.handle(
                        new RenewSubscriptionCommand(
                                subscriptionUuid,
                                Instant.parse(
                                        "2026-12-23T12:00:00Z"
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

    private Subscription createPendingSubscription(
            UUID subscriptionUuid
    ) {

        Instant now =
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
                        UUID.randomUUID()
                )
        );

        subscription.setPlanId(
                new PlanId(
                        UUID.randomUUID()
                )
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
}