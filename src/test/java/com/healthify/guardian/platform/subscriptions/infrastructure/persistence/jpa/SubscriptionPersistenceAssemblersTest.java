package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.*;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.EntitlementPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.EntitlementSetPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.PlanPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.SubscriptionPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.EntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionEntitlementPersistenceEntity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionPersistenceAssemblersTest {

    @Test
    void shouldRoundTripEntitlementWithoutLosingData() {

        Entitlement original = new Entitlement();

        original.setCode(
                new EntitlementCode("REAL_TIME_LOCATION")
        );

        original.setName(
                "Real-time location"
        );

        original.setDescription(
                "Allows real-time monitoring"
        );

        var persistence =
                EntitlementPersistenceAssembler
                        .toPersistenceFromDomain(original);

        Entitlement restored =
                EntitlementPersistenceAssembler
                        .toDomainFromPersistence(persistence);

        assertNotNull(restored);

        assertEquals(
                original.getCode(),
                restored.getCode()
        );

        assertEquals(
                original.getName(),
                restored.getName()
        );

        assertEquals(
                original.getDescription(),
                restored.getDescription()
        );
    }

    @Test
    void shouldRoundTripPlanWithoutLosingData() {

        UUID planUuid = UUID.randomUUID();

        Plan original = new Plan();

        original.setId(
                new PlanId(planUuid)
        );

        original.setName(
                "Guardian Plus Premium"
        );

        original.setPrice(
                new Money(
                        new BigDecimal("29.90"),
                        Currency.getInstance("PEN")
                )
        );

        original.setBillingCycle(
                BillingCycle.MONTHLY
        );

        original.setActive(true);

        original.setEntitlementCodes(
                Set.of(
                        new EntitlementCode("REAL_TIME_LOCATION"),
                        new EntitlementCode("HEALTH_MONITORING")
                )
        );

        var persistence =
                PlanPersistenceAssembler
                        .toPersistenceFromDomain(original);

        Plan restored =
                PlanPersistenceAssembler
                        .toDomainFromPersistence(
                                persistence,
                                original.getEntitlementCodes()
                        );

        assertNotNull(restored);

        assertEquals(
                original.getId(),
                restored.getId()
        );

        assertEquals(
                original.getName(),
                restored.getName()
        );

        assertEquals(
                original.getPrice(),
                restored.getPrice()
        );

        assertEquals(
                original.getBillingCycle(),
                restored.getBillingCycle()
        );

        assertEquals(
                original.isActive(),
                restored.isActive()
        );

        assertEquals(
                original.getEntitlementCodes(),
                restored.getEntitlementCodes()
        );
    }

    @Test
    void shouldRoundTripSubscriptionWithoutChangingIdentityOrState() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Instant periodStart =
                Instant.parse("2026-10-23T13:00:00Z");

        Instant periodEnd =
                Instant.parse("2026-11-23T13:00:00Z");

        Instant createdAt =
                Instant.parse("2026-10-23T12:00:00Z");

        Instant updatedAt =
                Instant.parse("2026-10-23T13:30:00Z");

        Subscription original =
                new Subscription();

        original.setId(
                new SubscriptionId(subscriptionUuid)
        );

        original.setSubscriberId(
                new SubscriberId(subscriberUuid)
        );

        original.setPlanId(
                new PlanId(planUuid)
        );

        original.setStatus(
                SubscriptionStatus.CANCELLATION_SCHEDULED
        );

        original.setCurrentPeriodStart(
                periodStart
        );

        original.setCurrentPeriodEnd(
                periodEnd
        );

        original.setCancelAtPeriodEnd(
                true
        );

        original.setCreatedAt(
                createdAt
        );

        original.setUpdatedAt(
                updatedAt
        );

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
                Set.of()
        );

        var planPersistence =
                PlanPersistenceAssembler
                        .toPersistenceFromDomain(plan);

        var subscriptionPersistence =
                SubscriptionPersistenceAssembler
                        .toPersistenceFromDomain(
                                original,
                                planPersistence
                        );

        Subscription restored =
                SubscriptionPersistenceAssembler
                        .toDomainFromPersistence(
                                subscriptionPersistence
                        );

        assertNotNull(restored);

        assertEquals(
                original.getId(),
                restored.getId()
        );

        assertEquals(
                original.getSubscriberId(),
                restored.getSubscriberId()
        );

        assertEquals(
                original.getPlanId(),
                restored.getPlanId()
        );

        assertEquals(
                SubscriptionStatus.CANCELLATION_SCHEDULED,
                restored.getStatus()
        );

        assertEquals(
                periodStart,
                restored.getCurrentPeriodStart()
        );

        assertEquals(
                periodEnd,
                restored.getCurrentPeriodEnd()
        );

        assertTrue(
                restored.isCancelAtPeriodEnd()
        );

        assertEquals(
                createdAt,
                restored.getCreatedAt()
        );

        assertEquals(
                updatedAt,
                restored.getUpdatedAt()
        );
    }

    @Test
    void shouldRebuildEntitlementSetFromPersistenceRelations() {

        UUID subscriptionUuid =
                UUID.randomUUID();

        UUID subscriberUuid =
                UUID.randomUUID();

        UUID planUuid =
                UUID.randomUUID();

        Instant now =
                Instant.parse("2026-10-23T15:00:00Z");

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
                Set.of()
        );

        var planPersistence =
                PlanPersistenceAssembler
                        .toPersistenceFromDomain(plan);

        Subscription subscription =
                new Subscription();

        subscription.setId(
                new SubscriptionId(subscriptionUuid)
        );

        subscription.setSubscriberId(
                new SubscriberId(subscriberUuid)
        );

        subscription.setPlanId(
                new PlanId(planUuid)
        );

        subscription.setStatus(
                SubscriptionStatus.ACTIVE
        );

        subscription.setCurrentPeriodStart(
                now
        );

        subscription.setCurrentPeriodEnd(
                Instant.parse("2026-11-23T15:00:00Z")
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

        var subscriptionPersistence =
                SubscriptionPersistenceAssembler
                        .toPersistenceFromDomain(
                                subscription,
                                planPersistence
                        );

        Entitlement location =
                new Entitlement();

        location.setCode(
                new EntitlementCode("REAL_TIME_LOCATION")
        );

        location.setName(
                "Real-time location"
        );

        location.setDescription(
                "Real-time monitoring"
        );

        Entitlement health =
                new Entitlement();

        health.setCode(
                new EntitlementCode("HEALTH_MONITORING")
        );

        health.setName(
                "Health monitoring"
        );

        health.setDescription(
                "Health monitoring capabilities"
        );

        EntitlementPersistenceEntity locationEntity =
                EntitlementPersistenceAssembler
                        .toPersistenceFromDomain(location);

        EntitlementPersistenceEntity healthEntity =
                EntitlementPersistenceAssembler
                        .toPersistenceFromDomain(health);

        EntitlementSet original =
                new EntitlementSet();

        original.setSubscriptionId(
                new SubscriptionId(subscriptionUuid)
        );

        original.setEntitlements(
                Set.of(
                        location,
                        health
                )
        );

        original.setUpdatedAt(
                now
        );

        var relations =
                EntitlementSetPersistenceAssembler
                        .toPersistenceFromDomain(
                                original,
                                subscriptionPersistence,
                                Map.of(
                                        "REAL_TIME_LOCATION",
                                        locationEntity,
                                        "HEALTH_MONITORING",
                                        healthEntity
                                )
                        );

        assertEquals(
                2,
                relations.size()
        );

        EntitlementSet restored =
                EntitlementSetPersistenceAssembler
                        .toDomainFromPersistence(
                                new SubscriptionId(subscriptionUuid),
                                relations
                        );

        assertNotNull(restored);

        assertEquals(
                original.getSubscriptionId(),
                restored.getSubscriptionId()
        );

        assertEquals(
                2,
                restored.getEntitlements().size()
        );

        assertTrue(
                restored.getEntitlements()
                        .stream()
                        .anyMatch(
                                entitlement ->
                                        entitlement
                                                .getCode()
                                                .equals(
                                                        new EntitlementCode(
                                                                "REAL_TIME_LOCATION"
                                                        )
                                                )
                        )
        );

        assertTrue(
                restored.getEntitlements()
                        .stream()
                        .anyMatch(
                                entitlement ->
                                        entitlement
                                                .getCode()
                                                .equals(
                                                        new EntitlementCode(
                                                                "HEALTH_MONITORING"
                                                        )
                                                )
                        )
        );

        assertEquals(
                now,
                restored.getUpdatedAt()
        );
    }
}