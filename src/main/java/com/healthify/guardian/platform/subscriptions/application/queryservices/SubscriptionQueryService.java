package com.healthify.guardian.platform.subscriptions.application.queryservices;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetActiveSubscriptionBySubscriberIdQuery;

import java.util.Optional;

public interface SubscriptionQueryService {

    Optional<Subscription> handle(
            GetActiveSubscriptionBySubscriberIdQuery query);
}