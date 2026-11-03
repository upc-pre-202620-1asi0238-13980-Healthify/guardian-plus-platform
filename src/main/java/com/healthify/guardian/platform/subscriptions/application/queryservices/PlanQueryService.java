package com.healthify.guardian.platform.subscriptions.application.queryservices;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetPlanByIdQuery;

import java.util.Optional;

public interface PlanQueryService {

    Optional<Plan> handle(
            GetPlanByIdQuery query);
}