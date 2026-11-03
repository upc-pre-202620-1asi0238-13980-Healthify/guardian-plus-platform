package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.BillingCycle;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.EntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanEntitlementId;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanEntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanPersistenceEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// ✅ SPRING BOOT 4
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SubscriptionPersistenceBlockATest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldPersistSubscriptionPlan() {

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

        entityManager.persistAndFlush(plan);
        entityManager.clear();

        PlanPersistenceEntity storedPlan =
                entityManager.find(
                        PlanPersistenceEntity.class,
                        planId
                );

        assertNotNull(storedPlan);
        assertEquals(planId, storedPlan.getId());
        assertEquals(
                "Guardian Plus Premium",
                storedPlan.getName()
        );
        assertEquals(
                0,
                new BigDecimal("29.90")
                        .compareTo(storedPlan.getPriceAmount())
        );
        assertEquals(
                "PEN",
                storedPlan.getPriceCurrency()
        );
        assertEquals(
                BillingCycle.MONTHLY,
                storedPlan.getBillingCycle()
        );
        assertTrue(storedPlan.isActive());
    }

    @Test
    void shouldPersistEntitlement() {

        EntitlementPersistenceEntity entitlement =
                new EntitlementPersistenceEntity(
                        "REAL_TIME_LOCATION",
                        "Real-time location",
                        "Allows real-time monitoring of the care recipient location"
                );

        entityManager.persistAndFlush(entitlement);
        entityManager.clear();

        EntitlementPersistenceEntity storedEntitlement =
                entityManager.find(
                        EntitlementPersistenceEntity.class,
                        "REAL_TIME_LOCATION"
                );

        assertNotNull(storedEntitlement);

        assertEquals(
                "REAL_TIME_LOCATION",
                storedEntitlement.getCode()
        );

        assertEquals(
                "Real-time location",
                storedEntitlement.getName()
        );

        assertEquals(
                "Allows real-time monitoring of the care recipient location",
                storedEntitlement.getDescription()
        );
    }

    @Test
    void shouldPersistPlanEntitlementRelationship() {

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

        EntitlementPersistenceEntity entitlement =
                new EntitlementPersistenceEntity(
                        "REAL_TIME_LOCATION",
                        "Real-time location",
                        "Allows real-time monitoring of the care recipient location"
                );

        entityManager.persist(plan);
        entityManager.persist(entitlement);

        PlanEntitlementPersistenceEntity planEntitlement =
                new PlanEntitlementPersistenceEntity(
                        plan,
                        entitlement
                );

        entityManager.persistAndFlush(planEntitlement);
        entityManager.clear();

        PlanEntitlementId relationId =
                new PlanEntitlementId(
                        planId,
                        "REAL_TIME_LOCATION"
                );

        PlanEntitlementPersistenceEntity storedRelation =
                entityManager.find(
                        PlanEntitlementPersistenceEntity.class,
                        relationId
                );

        assertNotNull(storedRelation);

        assertEquals(
                planId,
                storedRelation.getPlan().getId()
        );

        assertEquals(
                "REAL_TIME_LOCATION",
                storedRelation
                        .getEntitlement()
                        .getCode()
        );
    }
}