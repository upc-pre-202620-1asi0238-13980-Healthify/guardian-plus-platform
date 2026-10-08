package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating or changing a hydration plan.
 *
 * @param active             whether periodic hydration reminders are issued
 * @param dailyGoalGlasses   glasses of water per day (1 to 20)
 * @param intervalHours      hours between hydration reminders (1 to 12)
 * @param respectSleepWindow whether reminders inside the sleep window are suppressed
 */
public record ConfigureHydrationPlanResource(

        @NotNull(message = "{hydration-plan.active.blank}")
        Boolean active,

        @NotNull(message = "{hydration-plan.daily-goal-glasses.invalid}")
        Integer dailyGoalGlasses,

        @NotNull(message = "{hydration-plan.interval-hours.invalid}")
        Integer intervalHours,

        @NotNull(message = "{hydration-plan.respect-sleep-window.blank}")
        Boolean respectSleepWindow
) {
}
