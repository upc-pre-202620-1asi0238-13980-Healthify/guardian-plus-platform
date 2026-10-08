package com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.HydrationPlanCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfigureHydrationPlanCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.HydrationPlanRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

/**
 * Application service that executes hydration plan commands.
 */
@Service
public class HydrationPlanCommandServiceImpl implements HydrationPlanCommandService {

    private final HydrationPlanRepository hydrationPlanRepository;

    public HydrationPlanCommandServiceImpl(HydrationPlanRepository hydrationPlanRepository) {
        this.hydrationPlanRepository = hydrationPlanRepository;
    }

    @Override
    public Result<HydrationPlan, ApplicationError> handle(ConfigureHydrationPlanCommand command) {
        try {
            var existing = hydrationPlanRepository.findByPersonUnderCareId(new PersonUnderCareId(command.personUnderCareId()));
            var plan = existing.orElse(null);
            if (plan == null) {
                plan = new HydrationPlan(command);
            } else {
                plan.configure(command);
            }
            return Result.success(hydrationPlanRepository.save(plan));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "configure-hydration-plan", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }
}
