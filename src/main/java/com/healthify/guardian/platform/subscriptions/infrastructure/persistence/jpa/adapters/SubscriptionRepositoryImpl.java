package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers.SubscriptionPersistenceAssembler;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.PlanPersistenceRepository;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionPersistenceRepository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class SubscriptionRepositoryImpl
        implements SubscriptionRepository {

    private final SubscriptionPersistenceRepository subscriptionPersistenceRepository;
    private final PlanPersistenceRepository planPersistenceRepository;

    public SubscriptionRepositoryImpl(
            SubscriptionPersistenceRepository subscriptionPersistenceRepository,
            PlanPersistenceRepository planPersistenceRepository
    ) {
        this.subscriptionPersistenceRepository =
                subscriptionPersistenceRepository;

        this.planPersistenceRepository =
                planPersistenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findById(
            SubscriptionId id
    ) {
        return subscriptionPersistenceRepository
                .findById(id.value())
                .map(
                        SubscriptionPersistenceAssembler
                                ::toDomainFromPersistence
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Subscription> findActiveBySubscriberId(
            SubscriberId subscriberId
    ) {

        return subscriptionPersistenceRepository
                .findFirstBySubscriberIdAndStatusInOrderByCreatedAtDesc(
                        subscriberId.value(),
                        List.of(
                                SubscriptionStatus.ACTIVE,
                                SubscriptionStatus.CANCELLATION_SCHEDULED
                        )
                )
                .map(
                        SubscriptionPersistenceAssembler
                                ::toDomainFromPersistence
                );
    }

    @Override
    @Transactional
    public Subscription save(
            Subscription subscription
    ) {

        PlanPersistenceEntity plan =
                planPersistenceRepository
                        .findById(
                                subscription
                                        .getPlanId()
                                        .value()
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Subscription plan not found: "
                                                + subscription
                                                .getPlanId()
                                                .value()
                                )
                        );

        SubscriptionPersistenceEntity persistenceEntity =
                SubscriptionPersistenceAssembler
                        .toPersistenceFromDomain(
                                subscription,
                                plan
                        );

        SubscriptionPersistenceEntity saved =
                subscriptionPersistenceRepository
                        .save(persistenceEntity);

        return SubscriptionPersistenceAssembler
                .toDomainFromPersistence(saved);
    }
}