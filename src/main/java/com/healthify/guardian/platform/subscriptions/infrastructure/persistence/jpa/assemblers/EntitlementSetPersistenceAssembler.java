package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Entitlement;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.EntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionEntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class EntitlementSetPersistenceAssembler {

    private EntitlementSetPersistenceAssembler() {
    }

    public static EntitlementSet toDomainFromPersistence(
            SubscriptionId subscriptionId,
            List<SubscriptionEntitlementPersistenceEntity> relations
    ) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException(
                    "Subscription id is required"
            );
        }

        var entitlementSet = new EntitlementSet();

        entitlementSet.setSubscriptionId(
                subscriptionId
        );

        Set<Entitlement> entitlements =
                relations == null
                        ? Set.of()
                        : relations.stream()
                        .map(
                                relation ->
                                        EntitlementPersistenceAssembler
                                                .toDomainFromPersistence(
                                                        relation.getEntitlement()
                                                )
                        )
                        .collect(Collectors.toSet());

        entitlementSet.setEntitlements(
                entitlements
        );

        Instant updatedAt =
                relations == null || relations.isEmpty()
                        ? null
                        : relations.stream()
                        .map(
                                SubscriptionEntitlementPersistenceEntity::getUpdatedAt
                        )
                        .max(Instant::compareTo)
                        .orElse(null);

        entitlementSet.setUpdatedAt(
                updatedAt
        );

        return entitlementSet;
    }

    public static List<SubscriptionEntitlementPersistenceEntity>
    toPersistenceFromDomain(
            EntitlementSet entitlementSet,
            SubscriptionPersistenceEntity subscription,
            Map<String, EntitlementPersistenceEntity> entitlementEntities
    ) {
        if (entitlementSet == null) {
            return List.of();
        }

        if (subscription == null) {
            throw new IllegalArgumentException(
                    "Subscription persistence entity is required"
            );
        }

        return entitlementSet
                .getEntitlements()
                .stream()
                .map(entitlement -> {

                    String code =
                            entitlement
                                    .getCode()
                                    .value();

                    EntitlementPersistenceEntity entitlementEntity =
                            entitlementEntities.get(code);

                    if (entitlementEntity == null) {
                        throw new IllegalStateException(
                                "Entitlement not found: " + code
                        );
                    }

                    return new SubscriptionEntitlementPersistenceEntity(
                            subscription,
                            entitlementEntity,
                            entitlementSet.getUpdatedAt()
                    );
                })
                .toList();
    }
}