package com.healthify.guardian.platform.subscriptions.domain.model.queries;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;

public record GetActiveSubscriptionBySubscriberIdQuery(
        SubscriberId subscriberId) {

    public GetActiveSubscriptionBySubscriberIdQuery {
        if (subscriberId == null) {
            throw new IllegalArgumentException(
                    "subscription.subscriber-id.invalid");
        }
    }
}