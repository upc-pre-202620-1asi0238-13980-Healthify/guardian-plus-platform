package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanPersistenceEntity;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;

public final class SubscriptionPersistenceAssembler {

    private SubscriptionPersistenceAssembler() {
    }

    public static Subscription toDomainFromPersistence(
            SubscriptionPersistenceEntity entity
    ) {
        if (entity == null) {
            return null;
        }

        var subscription = new Subscription();

        subscription.setId(
                new SubscriptionId(entity.getId())
        );

        subscription.setSubscriberId(
                new SubscriberId(entity.getSubscriberId())
        );

        subscription.setPlanId(
                new PlanId(entity.getPlan().getId())
        );

        subscription.setStatus(
                entity.getStatus()
        );

        subscription.setCurrentPeriodStart(
                entity.getCurrentPeriodStart()
        );

        subscription.setCurrentPeriodEnd(
                entity.getCurrentPeriodEnd()
        );

        subscription.setCancelAtPeriodEnd(
                entity.isCancelAtPeriodEnd()
        );

        subscription.setCreatedAt(
                entity.getCreatedAt()
        );

        subscription.setUpdatedAt(
                entity.getUpdatedAt()
        );

        return subscription;
    }

    public static SubscriptionPersistenceEntity toPersistenceFromDomain(
            Subscription subscription,
            PlanPersistenceEntity plan
    ) {
        if (subscription == null) {
            return null;
        }

        if (plan == null) {
            throw new IllegalArgumentException(
                    "Plan persistence entity is required"
            );
        }

        return new SubscriptionPersistenceEntity(
                subscription.getId().value(),
                subscription.getSubscriberId().value(),
                plan,
                subscription.getStatus(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.isCancelAtPeriodEnd(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}