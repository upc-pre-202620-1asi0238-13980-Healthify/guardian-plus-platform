package com.healthify.guardian.platform.subscriptions.domain.repositories;

import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.SubscriptionId;

import java.util.Optional;

public interface EntitlementSetRepository {

    Optional<EntitlementSet> findBySubscriptionId(
            SubscriptionId subscriptionId);

    EntitlementSet save(
            EntitlementSet entitlementSet);
}