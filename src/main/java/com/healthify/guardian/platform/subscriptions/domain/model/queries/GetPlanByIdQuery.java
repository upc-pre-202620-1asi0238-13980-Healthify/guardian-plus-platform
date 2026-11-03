package com.healthify.guardian.platform.subscriptions.domain.model.queries;

import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;

public record GetPlanByIdQuery(
        PlanId planId) {

    public GetPlanByIdQuery {
        if (planId == null) {
            throw new IllegalArgumentException(
                    "subscription.plan-id.invalid");
        }
    }
}