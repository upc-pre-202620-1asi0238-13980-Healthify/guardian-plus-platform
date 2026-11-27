package com.healthify.guardian.platform.careroutineswellness.domain.model.events;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.HydrationPlanId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

/**
 * Raised when a hydration plan is created or changed.
 *
 * <p>Consumed internally by {@code HydrationPlanConfiguredEventHandler}, which replaces the pending
 * recurring hydration reminder when {@code scheduleChanged} is true.</p>
 *
 * @param active          whether periodic hydration reminders must be issued
 * @param intervalHours   hours between hydration reminders
 * @param scheduleChanged true when the plan was switched on or off or its interval changed
 */
public record HydrationPlanConfiguredEvent(
        HydrationPlanId hydrationPlanId,
        PersonUnderCareId personUnderCareId,
        boolean active,
        Integer intervalHours,
        boolean scheduleChanged) {

    public static HydrationPlanConfiguredEvent from(HydrationPlan plan, boolean scheduleChanged) {
        return new HydrationPlanConfiguredEvent(
                plan.getId(), plan.getPersonUnderCareId(), plan.isActive(), plan.getIntervalHours(), scheduleChanged);
    }
}
