package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPersistenceRepository
        extends JpaRepository<SubscriptionPersistenceEntity, UUID> {

    Optional<SubscriptionPersistenceEntity>
    findFirstBySubscriberIdAndStatusInOrderByCreatedAtDesc(
            UUID subscriberId,
            Collection<SubscriptionStatus> statuses
    );
}