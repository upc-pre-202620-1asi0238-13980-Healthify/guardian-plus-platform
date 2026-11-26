package com.healthify.guardian.platform.subscriptions.domain.repositories;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriberId;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;

import java.util.Optional;

public interface SubscriptionRepository {

    void acquireSubscriberLock(
            SubscriberId subscriberId);

    void acquireSubscriberLockForSubscription(
            SubscriptionId subscriptionId);

    Optional<Subscription> findById(
            SubscriptionId id);

    Optional<Subscription> findActiveBySubscriberId(
            SubscriberId subscriberId);

    Optional<Subscription> findPendingBySubscriberId(
            SubscriberId subscriberId);

    Subscription save(
            Subscription subscription);
}