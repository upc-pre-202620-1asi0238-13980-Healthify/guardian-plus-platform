package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.EntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionEntitlementId;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionEntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SubscriptionPersistenceBlockBTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistActiveSubscription() {

        UUID planId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID subscriberId = UUID.randomUUID();

        PlanPersistenceEntity plan =
                new PlanPersistenceEntity(
                        planId,
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        entityManager.persist(plan);

        Instant periodStart =
                Instant.parse("2026-10-23T13:00:00Z");

        Instant periodEnd =
                Instant.parse("2026-11-23T13:00:00Z");

        Instant createdAt =
                Instant.parse("2026-10-23T13:00:00Z");

        Instant updatedAt =
                Instant.parse("2026-10-23T13:30:00Z");

        SubscriptionPersistenceEntity subscription =
                new SubscriptionPersistenceEntity(
                        subscriptionId,
                        subscriberId,
                        plan,
                        SubscriptionStatus.ACTIVE,
                        periodStart,
                        periodEnd,
                        false,
                        createdAt,
                        updatedAt
                );

        entityManager.persistAndFlush(subscription);
        entityManager.clear();

        SubscriptionPersistenceEntity storedSubscription =
                entityManager.find(
                        SubscriptionPersistenceEntity.class,
                        subscriptionId
                );

        assertNotNull(storedSubscription);

        assertEquals(
                subscriptionId,
                storedSubscription.getId()
        );

        assertEquals(
                subscriberId,
                storedSubscription.getSubscriberId()
        );

        assertEquals(
                planId,
                storedSubscription.getPlan().getId()
        );

        assertEquals(
                SubscriptionStatus.ACTIVE,
                storedSubscription.getStatus()
        );

        assertEquals(
                periodStart,
                storedSubscription.getCurrentPeriodStart()
        );

        assertEquals(
                periodEnd,
                storedSubscription.getCurrentPeriodEnd()
        );

        assertFalse(
                storedSubscription.isCancelAtPeriodEnd()
        );

        assertEquals(
                createdAt,
                storedSubscription.getCreatedAt()
        );

        assertEquals(
                updatedAt,
                storedSubscription.getUpdatedAt()
        );
    }

    @Test
    void shouldAllowPendingSubscriptionWithoutActivePeriod() {

        UUID planId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID subscriberId = UUID.randomUUID();

        PlanPersistenceEntity plan =
                new PlanPersistenceEntity(
                        planId,
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        entityManager.persist(plan);

        Instant createdAt =
                Instant.parse("2026-10-23T14:00:00Z");

        SubscriptionPersistenceEntity subscription =
                new SubscriptionPersistenceEntity(
                        subscriptionId,
                        subscriberId,
                        plan,
                        SubscriptionStatus.PENDING,
                        null,
                        null,
                        false,
                        createdAt,
                        createdAt
                );

        entityManager.persistAndFlush(subscription);
        entityManager.clear();

        SubscriptionPersistenceEntity storedSubscription =
                entityManager.find(
                        SubscriptionPersistenceEntity.class,
                        subscriptionId
                );

        assertNotNull(storedSubscription);

        assertEquals(
                SubscriptionStatus.PENDING,
                storedSubscription.getStatus()
        );

        assertNull(
                storedSubscription.getCurrentPeriodStart()
        );

        assertNull(
                storedSubscription.getCurrentPeriodEnd()
        );

        assertFalse(
                storedSubscription.isCancelAtPeriodEnd()
        );
    }

    @Test
    void shouldPersistSubscriptionEntitlementRelationship() {

        UUID planId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        UUID subscriberId = UUID.randomUUID();

        PlanPersistenceEntity plan =
                new PlanPersistenceEntity(
                        planId,
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        EntitlementPersistenceEntity entitlement =
                new EntitlementPersistenceEntity(
                        "REAL_TIME_LOCATION",
                        "Real-time location",
                        "Allows real-time monitoring of the care recipient location"
                );

        entityManager.persist(plan);
        entityManager.persist(entitlement);

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

        entityManager.persist(subscription);

        SubscriptionEntitlementPersistenceEntity subscriptionEntitlement =
                new SubscriptionEntitlementPersistenceEntity(
                        subscription,
                        entitlement,
                        now
                );

        entityManager.persistAndFlush(subscriptionEntitlement);
        entityManager.clear();

        SubscriptionEntitlementId relationId =
                new SubscriptionEntitlementId(
                        subscriptionId,
                        "REAL_TIME_LOCATION"
                );

        SubscriptionEntitlementPersistenceEntity storedRelation =
                entityManager.find(
                        SubscriptionEntitlementPersistenceEntity.class,
                        relationId
                );

        assertNotNull(storedRelation);

        assertEquals(
                subscriptionId,
                storedRelation.getSubscription().getId()
        );

        assertEquals(
                "REAL_TIME_LOCATION",
                storedRelation.getEntitlement().getCode()
        );

        assertEquals(
                now,
                storedRelation.getUpdatedAt()
        );
    }
}