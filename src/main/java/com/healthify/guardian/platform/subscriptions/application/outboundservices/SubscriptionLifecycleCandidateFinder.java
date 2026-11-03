package com.healthify.guardian.platform.subscriptions.application.outboundservices;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SubscriptionLifecycleCandidateFinder {

    List<UUID> findDueSubscriptionIds(
            Instant evaluatedAt
    );
}