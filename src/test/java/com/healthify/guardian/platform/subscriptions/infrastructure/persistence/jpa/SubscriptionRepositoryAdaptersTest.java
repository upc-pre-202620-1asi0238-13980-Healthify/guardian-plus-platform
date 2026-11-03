package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.*;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementSetRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters.EntitlementSetRepositoryImpl;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters.PlanRepositoryImpl;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters.SubscriptionRepositoryImpl;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.EntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanEntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.EntitlementPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.PlanEntitlementPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.PlanPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionEntitlementPersistenceRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({
        PlanRepositoryImpl.class,
        SubscriptionRepositoryImpl.class,
        EntitlementSetRepositoryImpl.class
})
class SubscriptionRepositoryAdaptersTest {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private EntitlementSetRepository entitlementSetRepository;

    @Autowired
    private PlanPersistenceRepository planPersistenceRepository;

    @Autowired
    private EntitlementPersistenceRepository entitlementPersistenceRepository;

    @Autowired
    private PlanEntitlementPersistenceRepository planEntitlementPersistenceRepository;

    @Autowired
    private SubscriptionEntitlementPersistenceRepository
            subscriptionEntitlementPersistenceRepository;


    @Test
    void shouldLoadPlanWithItsEntitlementCodes() {

        UUID planUuid = UUID.randomUUID();

        PlanPersistenceEntity planEntity =
                new PlanPersistenceEntity(
                        planUuid,
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        EntitlementPersistenceEntity location =
                new EntitlementPersistenceEntity(
                        "REAL_TIME_LOCATION",
                        "Real-time location",
                        "Allows real-time location monitoring"
                );

        EntitlementPersistenceEntity health =
                new EntitlementPersistenceEntity(
                        "HEALTH_MONITORING",
                        "Health monitoring",
                        "Allows health monitoring"
                );

        planPersistenceRepository.save(planEntity);

        entitlementPersistenceRepository.save(location);
        entitlementPersistenceRepository.save(health);

        planEntitlementPersistenceRepository.save(
                new PlanEntitlementPersistenceEntity(
                        planEntity,
                        location
                )
        );

        planEntitlementPersistenceRepository.save(
                new PlanEntitlementPersistenceEntity(
                        planEntity,
                        health
                )
        );

        planEntitlementPersistenceRepository.flush();

        Plan result =
                planRepository
                        .findById(new PlanId(planUuid))
                        .orElseThrow();

        assertEquals(
                new PlanId(planUuid),
                result.getId()
        );

        assertEquals(
                "Guardian Plus Premium",
                result.getName()
        );

        assertTrue(result.isActive());

        assertEquals(
                2,
                result.getEntitlementCodes().size()
        );

        assertTrue(
                result.getEntitlementCodes().contains(
                        new EntitlementCode(
                                "REAL_TIME_LOCATION"
                        )
                )
        );

        assertTrue(
                result.getEntitlementCodes().contains(
                        new EntitlementCode(
                                "HEALTH_MONITORING"
                        )
                )
        );
    }


    @Test
    void shouldSaveAndFindSubscriptionThroughDomainRepository() {

        UUID planUuid = UUID.randomUUID();
        UUID subscriptionUuid = UUID.randomUUID();
        UUID subscriberUuid = UUID.randomUUID();

        createPlan(planUuid);

        Instant now =
                Instant.parse(
                        "2026-10-23T14:00:00Z"
                );

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        planUuid,
                        SubscriptionStatus.ACTIVE,
                        now,
                        Instant.parse(
                                "2026-11-23T14:00:00Z"
                        ),
                        false
                );

        Subscription saved =
                subscriptionRepository.save(
                        subscription
                );

        assertEquals(
                new SubscriptionId(subscriptionUuid),
                saved.getId()
        );

        assertEquals(
                SubscriptionStatus.ACTIVE,
                saved.getStatus()
        );

        Subscription restored =
                subscriptionRepository
                        .findById(
                                new SubscriptionId(
                                        subscriptionUuid
                                )
                        )
                        .orElseThrow();

        assertEquals(
                subscription.getId(),
                restored.getId()
        );

        assertEquals(
                subscription.getSubscriberId(),
                restored.getSubscriberId()
        );

        assertEquals(
                subscription.getPlanId(),
                restored.getPlanId()
        );

        assertEquals(
                SubscriptionStatus.ACTIVE,
                restored.getStatus()
        );
    }


    @Test
    void shouldFindCurrentSubscriptionForSubscriber() {

        UUID planUuid = UUID.randomUUID();
        UUID subscriberUuid = UUID.randomUUID();

        createPlan(planUuid);

        Subscription older =
                createSubscription(
                        UUID.randomUUID(),
                        subscriberUuid,
                        planUuid,
                        SubscriptionStatus.ACTIVE,
                        Instant.parse(
                                "2026-09-23T12:00:00Z"
                        ),
                        Instant.parse(
                                "2026-10-23T12:00:00Z"
                        ),
                        false
                );

        Subscription current =
                createSubscription(
                        UUID.randomUUID(),
                        subscriberUuid,
                        planUuid,
                        SubscriptionStatus.CANCELLATION_SCHEDULED,
                        Instant.parse(
                                "2026-10-23T12:00:00Z"
                        ),
                        Instant.parse(
                                "2026-11-23T12:00:00Z"
                        ),
                        true
                );

        subscriptionRepository.save(older);
        subscriptionRepository.save(current);

        Subscription result =
                subscriptionRepository
                        .findActiveBySubscriberId(
                                new SubscriberId(
                                        subscriberUuid
                                )
                        )
                        .orElseThrow();

        assertEquals(
                current.getId(),
                result.getId()
        );

        assertEquals(
                SubscriptionStatus.CANCELLATION_SCHEDULED,
                result.getStatus()
        );

        assertTrue(
                result.isCancelAtPeriodEnd()
        );
    }


    @Test
    void shouldSaveAndReplaceEffectiveEntitlements() {

        UUID planUuid = UUID.randomUUID();
        UUID subscriptionUuid = UUID.randomUUID();
        UUID subscriberUuid = UUID.randomUUID();

        createPlan(planUuid);

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        planUuid,
                        SubscriptionStatus.ACTIVE,
                        Instant.parse(
                                "2026-10-23T15:00:00Z"
                        ),
                        Instant.parse(
                                "2026-11-23T15:00:00Z"
                        ),
                        false
                );

        subscriptionRepository.save(subscription);

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

        EntitlementSet firstSet =
                new EntitlementSet();

        firstSet.setSubscriptionId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        firstSet.setEntitlements(
                Set.of(
                        location,
                        health
                )
        );

        firstSet.setUpdatedAt(
                Instant.parse(
                        "2026-10-23T15:30:00Z"
                )
        );

        entitlementSetRepository.save(
                firstSet
        );

        EntitlementSet stored =
                entitlementSetRepository
                        .findBySubscriptionId(
                                new SubscriptionId(
                                        subscriptionUuid
                                )
                        )
                        .orElseThrow();

        assertEquals(
                2,
                stored.getEntitlements().size()
        );

        EntitlementSet replacement =
                new EntitlementSet();

        replacement.setSubscriptionId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        replacement.setEntitlements(
                Set.of(location)
        );

        replacement.setUpdatedAt(
                Instant.parse(
                        "2026-10-23T16:00:00Z"
                )
        );

        entitlementSetRepository.save(
                replacement
        );

        EntitlementSet replaced =
                entitlementSetRepository
                        .findBySubscriptionId(
                                new SubscriptionId(
                                        subscriptionUuid
                                )
                        )
                        .orElseThrow();

        assertEquals(
                1,
                replaced.getEntitlements().size()
        );

        assertTrue(
                replaced.getEntitlements()
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

        assertFalse(
                replaced.getEntitlements()
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
                1,
                subscriptionEntitlementPersistenceRepository
                        .findAllBySubscription_Id(
                                subscriptionUuid
                        )
                        .size()
        );
    }


    @Test
    void shouldSupportSubscriptionWithNoEffectiveEntitlements() {

        UUID planUuid = UUID.randomUUID();
        UUID subscriptionUuid = UUID.randomUUID();
        UUID subscriberUuid = UUID.randomUUID();

        createPlan(planUuid);

        Subscription subscription =
                createSubscription(
                        subscriptionUuid,
                        subscriberUuid,
                        planUuid,
                        SubscriptionStatus.ACTIVE,
                        Instant.parse(
                                "2026-10-23T17:00:00Z"
                        ),
                        Instant.parse(
                                "2026-11-23T17:00:00Z"
                        ),
                        false
                );

        subscriptionRepository.save(
                subscription
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
                Set.of(health)
        );

        entitlementSet.setUpdatedAt(
                Instant.parse(
                        "2026-10-23T17:30:00Z"
                )
        );

        entitlementSetRepository.save(
                entitlementSet
        );

        EntitlementSet emptySet =
                new EntitlementSet();

        emptySet.setSubscriptionId(
                new SubscriptionId(
                        subscriptionUuid
                )
        );

        emptySet.setEntitlements(
                Set.of()
        );

        emptySet.setUpdatedAt(
                Instant.parse(
                        "2026-10-23T18:00:00Z"
                )
        );

        entitlementSetRepository.save(
                emptySet
        );

        assertTrue(
                subscriptionEntitlementPersistenceRepository
                        .findAllBySubscription_Id(
                                subscriptionUuid
                        )
                        .isEmpty()
        );

        EntitlementSet restored =
                entitlementSetRepository
                        .findBySubscriptionId(
                                new SubscriptionId(
                                        subscriptionUuid
                                )
                        )
                        .orElseThrow();

        assertTrue(
                restored
                        .getEntitlements()
                        .isEmpty()
        );
    }


    private void createPlan(
            UUID planUuid
    ) {

        PlanPersistenceEntity plan =
                new PlanPersistenceEntity(
                        planUuid,
                        "Guardian Plus Premium",
                        new BigDecimal("29.90"),
                        "PEN",
                        BillingCycle.MONTHLY,
                        true
                );

        planPersistenceRepository.saveAndFlush(
                plan
        );
    }


    private Subscription createSubscription(
            UUID subscriptionUuid,
            UUID subscriberUuid,
            UUID planUuid,
            SubscriptionStatus status,
            Instant periodStart,
            Instant periodEnd,
            boolean cancelAtPeriodEnd
    ) {

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

        subscription.setCurrentPeriodStart(
                periodStart
        );

        subscription.setCurrentPeriodEnd(
                periodEnd
        );

        subscription.setCancelAtPeriodEnd(
                cancelAtPeriodEnd
        );

        subscription.setCreatedAt(
                periodStart
        );

        subscription.setUpdatedAt(
                periodStart
        );

        return subscription;
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