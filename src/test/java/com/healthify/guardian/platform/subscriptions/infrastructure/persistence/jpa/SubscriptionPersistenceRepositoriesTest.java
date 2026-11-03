package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.*;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SubscriptionPersistenceRepositoriesTest {

    @Autowired
    private PlanPersistenceRepository planRepository;

    @Autowired
    private EntitlementPersistenceRepository entitlementRepository;

    @Autowired
    private PlanEntitlementPersistenceRepository planEntitlementRepository;

    @Autowired
    private SubscriptionPersistenceRepository subscriptionRepository;

    @Autowired
    private SubscriptionEntitlementPersistenceRepository
            subscriptionEntitlementRepository;

    @Test
    void shouldFindOnlyActivePlans() {

        PlanPersistenceEntity activePlan =
                new PlanPersistenceEntity(
                        UUID.randomUUID(),
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        PlanPersistenceEntity inactivePlan =
                new PlanPersistenceEntity(
                        UUID.randomUUID(),
                        "Guardian Plus Legacy",
                        new BigDecimal("19.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        false
                );

        planRepository.save(activePlan);
        planRepository.save(inactivePlan);
        planRepository.flush();

        List<PlanPersistenceEntity> result =
                planRepository.findAllByActiveTrue();

        assertEquals(1, result.size());

        assertEquals(
                activePlan.getId(),
                result.getFirst().getId()
        );

        assertTrue(result.getFirst().isActive());
    }

    @Test
    void shouldSaveAndFindEntitlementByCode() {

        EntitlementPersistenceEntity entitlement =
                new EntitlementPersistenceEntity(
                        "REAL_TIME_LOCATION",
                        "Real-time location",
                        "Allows real-time monitoring of the care recipient location"
                );

        entitlementRepository.saveAndFlush(entitlement);

        EntitlementPersistenceEntity storedEntitlement =
                entitlementRepository
                        .findById("REAL_TIME_LOCATION")
                        .orElseThrow();

        assertEquals(
                "REAL_TIME_LOCATION",
                storedEntitlement.getCode()
        );

        assertEquals(
                "Real-time location",
                storedEntitlement.getName()
        );
    }

    @Test
    void shouldFindEntitlementsAssignedToPlan() {

        UUID planId = UUID.randomUUID();

        PlanPersistenceEntity plan =
                new PlanPersistenceEntity(
                        planId,
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        EntitlementPersistenceEntity locationEntitlement =
                new EntitlementPersistenceEntity(
                        "REAL_TIME_LOCATION",
                        "Real-time location",
                        "Real-time monitoring"
                );

        EntitlementPersistenceEntity alertsEntitlement =
                new EntitlementPersistenceEntity(
                        "EMERGENCY_ALERTS",
                        "Emergency alerts",
                        "Enables emergency notifications"
                );

        planRepository.save(plan);

        entitlementRepository.save(locationEntitlement);
        entitlementRepository.save(alertsEntitlement);

        planEntitlementRepository.save(
                new PlanEntitlementPersistenceEntity(
                        plan,
                        locationEntitlement
                )
        );

        planEntitlementRepository.save(
                new PlanEntitlementPersistenceEntity(
                        plan,
                        alertsEntitlement
                )
        );

        planEntitlementRepository.flush();

        List<PlanEntitlementPersistenceEntity> relations =
                planEntitlementRepository
                        .findAllByPlan_Id(planId);

        assertEquals(2, relations.size());

        assertTrue(
                relations.stream()
                        .allMatch(
                                relation ->
                                        relation
                                                .getPlan()
                                                .getId()
                                                .equals(planId)
                        )
        );

        assertTrue(
                relations.stream()
                        .anyMatch(
                                relation ->
                                        relation
                                                .getEntitlement()
                                                .getCode()
                                                .equals("REAL_TIME_LOCATION")
                        )
        );

        assertTrue(
                relations.stream()
                        .anyMatch(
                                relation ->
                                        relation
                                                .getEntitlement()
                                                .getCode()
                                                .equals("EMERGENCY_ALERTS")
                        )
        );
    }

    @Test
    void shouldFindMostRecentCurrentSubscriptionForSubscriber() {

        UUID subscriberId = UUID.randomUUID();

        PlanPersistenceEntity plan =
                new PlanPersistenceEntity(
                        UUID.randomUUID(),
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        planRepository.save(plan);

        Instant firstCreatedAt =
                Instant.parse("2026-09-23T12:00:00Z");

        Instant secondCreatedAt =
                Instant.parse("2026-10-23T12:00:00Z");

        SubscriptionPersistenceEntity firstSubscription =
                new SubscriptionPersistenceEntity(
                        UUID.randomUUID(),
                        subscriberId,
                        plan,
                        SubscriptionStatus.ACTIVE,
                        firstCreatedAt,
                        Instant.parse("2026-10-23T12:00:00Z"),
                        false,
                        firstCreatedAt,
                        firstCreatedAt
                );

        SubscriptionPersistenceEntity currentSubscription =
                new SubscriptionPersistenceEntity(
                        UUID.randomUUID(),
                        subscriberId,
                        plan,
                        SubscriptionStatus.CANCELLATION_SCHEDULED,
                        secondCreatedAt,
                        Instant.parse("2026-11-23T12:00:00Z"),
                        true,
                        secondCreatedAt,
                        secondCreatedAt
                );

        SubscriptionPersistenceEntity expiredSubscription =
                new SubscriptionPersistenceEntity(
                        UUID.randomUUID(),
                        subscriberId,
                        plan,
                        SubscriptionStatus.EXPIRED,
                        Instant.parse("2026-08-23T12:00:00Z"),
                        Instant.parse("2026-09-23T12:00:00Z"),
                        false,
                        Instant.parse("2026-08-23T12:00:00Z"),
                        Instant.parse("2026-09-23T12:00:00Z")
                );

        subscriptionRepository.save(firstSubscription);
        subscriptionRepository.save(currentSubscription);
        subscriptionRepository.save(expiredSubscription);
        subscriptionRepository.flush();

        SubscriptionPersistenceEntity result =
                subscriptionRepository
                        .findFirstBySubscriberIdAndStatusInOrderByCreatedAtDesc(
                                subscriberId,
                                List.of(
                                        SubscriptionStatus.ACTIVE,
                                        SubscriptionStatus.CANCELLATION_SCHEDULED
                                )
                        )
                        .orElseThrow();

        assertEquals(
                currentSubscription.getId(),
                result.getId()
        );

        assertEquals(
                SubscriptionStatus.CANCELLATION_SCHEDULED,
                result.getStatus()
        );

        assertTrue(result.isCancelAtPeriodEnd());
    }

    @Test
    void shouldFindEffectiveEntitlementsForSubscription() {

        UUID subscriptionId = UUID.randomUUID();
        UUID subscriberId = UUID.randomUUID();

        PlanPersistenceEntity plan =
                new PlanPersistenceEntity(
                        UUID.randomUUID(),
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        EntitlementPersistenceEntity locationEntitlement =
                new EntitlementPersistenceEntity(
                        "REAL_TIME_LOCATION",
                        "Real-time location",
                        "Real-time monitoring"
                );

        EntitlementPersistenceEntity healthEntitlement =
                new EntitlementPersistenceEntity(
                        "HEALTH_MONITORING",
                        "Health monitoring",
                        "Enables health monitoring capabilities"
                );

        planRepository.save(plan);

        entitlementRepository.save(locationEntitlement);
        entitlementRepository.save(healthEntitlement);

        Instant now =
                Instant.parse("2026-10-23T15:00:00Z");

        SubscriptionPersistenceEntity subscription =
                new SubscriptionPersistenceEntity(
                        subscriptionId,
                        subscriberId,
                        plan,
                        SubscriptionStatus.ACTIVE,
                        now,
                        Instant.parse("2026-11-23T15:00:00Z"),
                        false,
                        now,
                        now
                );

        subscriptionRepository.save(subscription);

        subscriptionEntitlementRepository.save(
                new SubscriptionEntitlementPersistenceEntity(
                        subscription,
                        locationEntitlement,
                        now
                )
        );

        subscriptionEntitlementRepository.save(
                new SubscriptionEntitlementPersistenceEntity(
                        subscription,
                        healthEntitlement,
                        now
                )
        );

        subscriptionEntitlementRepository.flush();

        List<SubscriptionEntitlementPersistenceEntity> result =
                subscriptionEntitlementRepository
                        .findAllBySubscription_Id(subscriptionId);

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(
                                relation ->
                                        relation
                                                .getSubscription()
                                                .getId()
                                                .equals(subscriptionId)
                        )
        );

        assertTrue(
                result.stream()
                        .anyMatch(
                                relation ->
                                        relation
                                                .getEntitlement()
                                                .getCode()
                                                .equals("REAL_TIME_LOCATION")
                        )
        );

        assertTrue(
                result.stream()
                        .anyMatch(
                                relation ->
                                        relation
                                                .getEntitlement()
                                                .getCode()
                                                .equals("HEALTH_MONITORING")
                        )
        );
    }
}