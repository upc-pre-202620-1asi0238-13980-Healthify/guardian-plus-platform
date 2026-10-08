package com.healthify.guardian.platform.careroutineswellness.application.commandservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfigureHydrationPlanCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code HydrationPlan} aggregate.
 */
public interface HydrationPlanCommandService {

    /**
     * Creates or changes the hydration plan of a person under care.
     *
     * @param command the new configuration
     * @return the saved hydration plan or an application error
     */
    Result<HydrationPlan, ApplicationError> handle(ConfigureHydrationPlanCommand command);
}
