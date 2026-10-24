package com.healthify.guardian.platform.subscriptions.interfaces.rest.transform;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.interfaces.rest.resources.SubscriptionResource;

public final class SubscriptionResourceFromEntityAssembler {

    private SubscriptionResourceFromEntityAssembler() {
    }

    public static SubscriptionResource toResourceFromEntity(
            Subscription subscription
    ) {

        return new SubscriptionResource(
                subscription.getId().value(),
                subscription.getSubscriberId().value(),
                subscription.getPlanId().value(),
                subscription.getStatus().name(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.isCancelAtPeriodEnd(),
                subscription.getCreatedAt(),
                subscription.getUpdatedAt()
        );
    }
}