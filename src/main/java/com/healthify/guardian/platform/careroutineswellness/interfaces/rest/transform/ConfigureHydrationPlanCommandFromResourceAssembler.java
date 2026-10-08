package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfigureHydrationPlanCommand;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ConfigureHydrationPlanResource;

import java.util.UUID;

/**
 * Assembler to convert a {@link ConfigureHydrationPlanResource} to a {@link ConfigureHydrationPlanCommand}.
 */
public final class ConfigureHydrationPlanCommandFromResourceAssembler {

    private ConfigureHydrationPlanCommandFromResourceAssembler() {
    }

    public static ConfigureHydrationPlanCommand toCommandFromResource(
            UUID personUnderCareId, ConfigureHydrationPlanResource resource) {
        return new ConfigureHydrationPlanCommand(
                personUnderCareId,
                resource.active(),
                resource.dailyGoalGlasses(),
                resource.intervalHours(),
                resource.respectSleepWindow());
    }
}
