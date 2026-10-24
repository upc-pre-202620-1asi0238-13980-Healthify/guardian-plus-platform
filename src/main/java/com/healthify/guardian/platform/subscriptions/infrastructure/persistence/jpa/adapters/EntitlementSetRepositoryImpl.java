package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.repositories.EntitlementSetRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.EntitlementPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.EntitlementSetPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.EntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionEntitlementPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.EntitlementPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionEntitlementPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionPersistenceRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class EntitlementSetRepositoryImpl
        implements EntitlementSetRepository {

    private final SubscriptionPersistenceRepository subscriptionPersistenceRepository;

    private final EntitlementPersistenceRepository entitlementPersistenceRepository;

    private final SubscriptionEntitlementPersistenceRepository
            subscriptionEntitlementPersistenceRepository;

    public EntitlementSetRepositoryImpl(
            SubscriptionPersistenceRepository subscriptionPersistenceRepository,
            EntitlementPersistenceRepository entitlementPersistenceRepository,
            SubscriptionEntitlementPersistenceRepository
                    subscriptionEntitlementPersistenceRepository
    ) {
        this.subscriptionPersistenceRepository =
                subscriptionPersistenceRepository;

        this.entitlementPersistenceRepository =
                entitlementPersistenceRepository;

        this.subscriptionEntitlementPersistenceRepository =
                subscriptionEntitlementPersistenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntitlementSet> findBySubscriptionId(
            SubscriptionId subscriptionId
    ) {

        Optional<SubscriptionPersistenceEntity> subscription =
                subscriptionPersistenceRepository
                        .findById(subscriptionId.value());

        if (subscription.isEmpty()) {
            return Optional.empty();
        }

        List<SubscriptionEntitlementPersistenceEntity> relations =
                subscriptionEntitlementPersistenceRepository
                        .findAllBySubscription_Id(
                                subscriptionId.value()
                        );

        if (relations.isEmpty()) {

            EntitlementSet emptySet =
                    new EntitlementSet();

            emptySet.setSubscriptionId(
                    subscriptionId
            );

            emptySet.setEntitlements(
                    Set.of()
            );

            emptySet.setUpdatedAt(
                    subscription.get().getUpdatedAt()
            );

            return Optional.of(emptySet);
        }

        return Optional.of(
                EntitlementSetPersistenceAssembler
                        .toDomainFromPersistence(
                                subscriptionId,
                                relations
                        )
        );
    }

    @Override
    @Transactional
    public EntitlementSet save(
            EntitlementSet entitlementSet
    ) {

        SubscriptionPersistenceEntity subscription =
                subscriptionPersistenceRepository
                        .findById(
                                entitlementSet
                                        .getSubscriptionId()
                                        .value()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Subscription not found: "
                                                + entitlementSet
                                                .getSubscriptionId()
                                                .value()
                                )
                        );

        Map<String, EntitlementPersistenceEntity>
                entitlementEntities =
                new HashMap<>();

        entitlementSet
                .getEntitlements()
                .forEach(entitlement -> {

                    String code =
                            entitlement
                                    .getCode()
                                    .value();

                    EntitlementPersistenceEntity entity =
                            entitlementPersistenceRepository
                                    .findById(code)
                                    .orElseGet(() ->
                                            entitlementPersistenceRepository
                                                    .save(
                                                            EntitlementPersistenceAssembler
                                                                    .toPersistenceFromDomain(
                                                                            entitlement
                                                                    )
                                                    )
                                    );

                    entitlementEntities.put(
                            code,
                            entity
                    );
                });

        subscriptionEntitlementPersistenceRepository
                .deleteAllBySubscription_Id(
                        entitlementSet
                                .getSubscriptionId()
                                .value()
                );

        subscriptionEntitlementPersistenceRepository
                .flush();

        List<SubscriptionEntitlementPersistenceEntity> relations =
                EntitlementSetPersistenceAssembler
                        .toPersistenceFromDomain(
                                entitlementSet,
                                subscription,
                                entitlementEntities
                        );

        List<SubscriptionEntitlementPersistenceEntity> savedRelations =
                subscriptionEntitlementPersistenceRepository
                        .saveAll(relations);

        subscriptionEntitlementPersistenceRepository
                .flush();

        if (savedRelations.isEmpty()) {
            return entitlementSet;
        }

        return EntitlementSetPersistenceAssembler
                .toDomainFromPersistence(
                        entitlementSet.getSubscriptionId(),
                        savedRelations
                );
    }
}