package com.healthify.guardian.platform.subscriptions.application.internal.queryservices;

import com.healthify.guardian.platform.subscriptions.application.queryservices.PlanQueryService;
import com.healthify.guardian.platform.subscriptions.domain.model.entities.Plan;
import com.healthify.guardian.platform.subscriptions.domain.model.queries.GetPlanByIdQuery;
import com.healthify.guardian.platform.subscriptions.domain.repositories.PlanRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlanQueryServiceImpl
        implements PlanQueryService {

    private final PlanRepository planRepository;

    public PlanQueryServiceImpl(
            PlanRepository planRepository) {

        this.planRepository = planRepository;
    }

    @Override
    public Optional<Plan> handle(
            GetPlanByIdQuery query) {

        return planRepository.findById(
                query.planId());
    }
}