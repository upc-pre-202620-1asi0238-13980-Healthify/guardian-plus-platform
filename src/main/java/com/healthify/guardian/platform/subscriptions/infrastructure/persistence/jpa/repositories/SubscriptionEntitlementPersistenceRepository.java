package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionEntitlementId;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionEntitlementPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionEntitlementPersistenceRepository
        extends JpaRepository<
        SubscriptionEntitlementPersistenceEntity,
        SubscriptionEntitlementId
        > {

    List<SubscriptionEntitlementPersistenceEntity>
    findAllBySubscription_Id(UUID subscriptionId);

    void deleteAllBySubscription_Id(UUID subscriptionId);
}