package com.healthify.guardian.platform.subscriptions.application.queryservices;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetAvailableEntitlementsQuery;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetSubscriptionStatusQuery;

public interface SubscriptionApplicationQueryService {

    Result<Subscription, ApplicationError> handle(
            GetSubscriptionStatusQuery query
    );

    Result<EntitlementSet, ApplicationError> handle(
            GetAvailableEntitlementsQuery query
    );
}