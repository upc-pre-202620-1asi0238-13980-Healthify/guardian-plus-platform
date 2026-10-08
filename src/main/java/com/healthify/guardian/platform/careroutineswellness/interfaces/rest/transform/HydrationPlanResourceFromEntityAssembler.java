package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.HydrationProgress;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.HydrationPlanResource;

/**
 * Assembler that combines a {@link HydrationPlan} and today's {@link HydrationProgress} into a
 * {@link HydrationPlanResource}.
 */
public final class HydrationPlanResourceFromEntityAssembler {

    private HydrationPlanResourceFromEntityAssembler() {
    }

    public static HydrationPlanResource toResourceFromEntity(
            HydrationPlan plan, HydrationProgress progress, SleepWindow sleepWindow) {
        return new HydrationPlanResource(
                plan.getId().value(),
                plan.getPersonUnderCareId().value(),
                plan.isActive(),
                plan.getDailyGoalGlasses(),
                plan.getIntervalHours(),
                plan.respectsSleepWindow(),
                sleepWindow.start(),
                sleepWindow.end(),
                progress.glassesConsumed(),
                Math.max(0, plan.getDailyGoalGlasses() - progress.glassesConsumed()),
                progress.nextReminderAt());
    }
}
