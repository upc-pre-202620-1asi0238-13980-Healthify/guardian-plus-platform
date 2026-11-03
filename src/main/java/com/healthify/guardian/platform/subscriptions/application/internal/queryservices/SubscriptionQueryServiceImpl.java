package com.healthify.guardian.platform.subscriptions.application.internal.queryservices;

import com.healthify.guardian.platform.subscriptions.application.queryservices.SubscriptionQueryService;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetActiveSubscriptionBySubscriberIdQuery;
import com.healthify.guardian.platform.subscriptions.domain.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubscriptionQueryServiceImpl
        implements SubscriptionQueryService {

    private final SubscriptionRepository
            subscriptionRepository;

    public SubscriptionQueryServiceImpl(
            SubscriptionRepository subscriptionRepository) {

        this.subscriptionRepository =
                subscriptionRepository;
    }

    @Override
    public Optional<Subscription> handle(
            GetActiveSubscriptionBySubscriberIdQuery query) {

        return subscriptionRepository
                .findActiveBySubscriberId(
                        query.subscriberId());
    }
}