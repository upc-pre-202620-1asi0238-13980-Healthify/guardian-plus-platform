package com.healthify.guardian.platform.subscriptions.application.internal.commandservices;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.SyncEntitlementsFromPlanCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.*;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementCatalogRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementSetRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;

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

class EntitlementSyncFromPlanServiceTest {

    @Mock
    private EntitlementSetRepository entitlementSetRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private EntitlementCatalogRepository entitlementCatalogRepository;

    private EntitlementSetCommandServiceImpl entitlementSetCommandService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        entitlementSetCommandService =
                new EntitlementSetCommandServiceImpl(
                        entitlementSetRepository,
                        planRepository,
                        entitlementCatalogRepository
                );

        when(entitlementSetRepository.save(
                any(EntitlementSet.class)
        )).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );
    }

    @Test
    void shouldSynchronizeEntitlementsFromPlan() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        EntitlementCode locationCode =
                new EntitlementCode(
                        "REAL_TIME_LOCATION"
                );

        EntitlementCode healthCode =
                new EntitlementCode(
                        "HEALTH_MONITORING"
                );

        Plan plan =
                createPlan(
                        planUuid,
                        Set.of(
                                locationCode,
                                healthCode
                        )
                );

        EntitlementSet existingSet =
                createEntitlementSet(
                        subscriptionUuid
                );

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

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(plan)
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(existingSet)
        );

        when(entitlementCatalogRepository.findAllByCodes(
                plan.getEntitlementCodes()
        )).thenReturn(
                Set.of(
                        location,
                        health
                )
        );

        var result =
                entitlementSetCommandService.handle(
                        new SyncEntitlementsFromPlanCommand(
                                subscriptionUuid,
                                planUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        EntitlementSet updated =
                result.toOptional()
                        .orElseThrow();

        assertEquals(
                2,
                updated.getEntitlements().size()
        );

        assertTrue(
                updated.contains(
                        locationCode
                )
        );

        assertTrue(
                updated.contains(
                        healthCode
                )
        );

        assertNotNull(
                updated.getUpdatedAt()
        );

        verify(entitlementSetRepository)
                .save(existingSet);
    }

    @Test
    void shouldAllowPlanWithoutEntitlements() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        Plan plan =
                createPlan(
                        planUuid,
                        Set.of()
                );

        EntitlementSet existingSet =
                createEntitlementSet(
                        subscriptionUuid
                );

        existingSet.replaceWith(
                Set.of(
                        createEntitlement(
                                "OLD_FEATURE",
                                "Old feature"
                        )
                ),
                Instant.parse(
                        "2026-10-22T12:00:00Z"
                )
        );

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(plan)
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(existingSet)
        );

        when(entitlementCatalogRepository.findAllByCodes(
                Set.of()
        )).thenReturn(
                Set.of()
        );

        var result =
                entitlementSetCommandService.handle(
                        new SyncEntitlementsFromPlanCommand(
                                subscriptionUuid,
                                planUuid
                        )
                );

        assertTrue(
                result.isSuccess()
        );

        EntitlementSet updated =
                result.toOptional()
                        .orElseThrow();

        assertTrue(
                updated.getEntitlements().isEmpty()
        );

        verify(entitlementSetRepository)
                .save(existingSet);
    }

    @Test
    void shouldFailWhenPlanDoesNotExist() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                entitlementSetCommandService.handle(
                        new SyncEntitlementsFromPlanCommand(
                                subscriptionUuid,
                                planUuid
                        )
                );

        assertTrue(
                result.isFailure()
        );

        verify(
                entitlementSetRepository,
                never()
        ).save(
                any(EntitlementSet.class)
        );
    }

    @Test
    void shouldFailWhenSubscriptionEntitlementSetDoesNotExist() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        Plan plan =
                createPlan(
                        planUuid,
                        Set.of(
                                new EntitlementCode(
                                        "REAL_TIME_LOCATION"
                                )
                        )
                );

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(plan)
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.empty()
        );

        var result =
                entitlementSetCommandService.handle(
                        new SyncEntitlementsFromPlanCommand(
                                subscriptionUuid,
                                planUuid
                        )
                );

        assertTrue(
                result.isFailure()
        );

        verify(
                entitlementSetRepository,
                never()
        ).save(
                any(EntitlementSet.class)
        );
    }

    @Test
    void shouldFailWhenPlanReferencesUnknownEntitlement() {

        UUID subscriptionUuid = UUID.randomUUID();
        UUID planUuid = UUID.randomUUID();

        EntitlementCode knownCode =
                new EntitlementCode(
                        "REAL_TIME_LOCATION"
                );

        EntitlementCode unknownCode =
                new EntitlementCode(
                        "UNKNOWN_FEATURE"
                );

        Plan plan =
                createPlan(
                        planUuid,
                        Set.of(
                                knownCode,
                                unknownCode
                        )
                );

        EntitlementSet existingSet =
                createEntitlementSet(
                        subscriptionUuid
                );

        when(planRepository.findById(
                new PlanId(planUuid)
        )).thenReturn(
                Optional.of(plan)
        );

        when(entitlementSetRepository.findBySubscriptionId(
                new SubscriptionId(subscriptionUuid)
        )).thenReturn(
                Optional.of(existingSet)
        );

        when(entitlementCatalogRepository.findAllByCodes(
                plan.getEntitlementCodes()
        )).thenReturn(
                Set.of(
                        createEntitlement(
                                "REAL_TIME_LOCATION",
                                "Real-time location"
                        )
                )
        );

        var result =
                entitlementSetCommandService.handle(
                        new SyncEntitlementsFromPlanCommand(
                                subscriptionUuid,
                                planUuid
                        )
                );

        assertTrue(
                result.isFailure()
        );

        verify(
                entitlementSetRepository,
                never()
        ).save(
                any(EntitlementSet.class)
        );
    }

    private Plan createPlan(
            UUID planUuid,
            Set<EntitlementCode> entitlementCodes
    ) {

        Plan plan = new Plan();

        plan.setId(
                new PlanId(planUuid)
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

        plan.setActive(true);

        plan.setEntitlementCodes(
                entitlementCodes
        );

        return plan;
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
                Set.of()
        );

        entitlementSet.setUpdatedAt(
                Instant.parse(
                        "2026-10-22T10:00:00Z"
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