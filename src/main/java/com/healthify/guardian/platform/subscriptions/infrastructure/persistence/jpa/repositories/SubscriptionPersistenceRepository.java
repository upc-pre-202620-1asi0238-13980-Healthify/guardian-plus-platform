package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionStatus;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(propagation = Propagation.MANDATORY)
    @Query(
            value = """
                    SELECT subscriber_id
                    FROM subscriptions
                    WHERE id = :subscriptionId
                    """,
            nativeQuery = true
    )
    Optional<UUID> findSubscriberIdBySubscriptionId(
            @Param("subscriptionId") UUID subscriptionId
    );

    @Transactional(propagation = Propagation.MANDATORY)
    @Query(
            value = """
                    SELECT 1
                    FROM pg_advisory_xact_lock(
                        hashtextextended(
                            'guardian-plus:subscriptions:' ||
                            CAST(:subscriberId AS text),
                            0
                        )
                    )
                    """,
            nativeQuery = true
    )
    Integer acquireSubscriberLock(
            @Param("subscriberId") UUID subscriberId
    );
}