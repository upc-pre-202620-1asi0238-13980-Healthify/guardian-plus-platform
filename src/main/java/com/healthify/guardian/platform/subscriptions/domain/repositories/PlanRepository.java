package com.healthify.guardian.platform.subscriptions.domain.repositories;

import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.valueobjects.PlanId;

import java.util.List;
import java.util.Optional;

public interface PlanRepository {

    Optional<Plan> findById(
            PlanId id);

    List<Plan> findActivePlans();
}