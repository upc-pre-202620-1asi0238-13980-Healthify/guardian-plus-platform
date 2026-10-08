package com.healthify.guardian.platform.careroutineswellness.domain.model.commands;

import java.util.UUID;

/**
 * Command to create or change the hydration plan of a person under care.
 *
 * @param personUnderCareId  the person the plan belongs to
 * @param active             whether periodic hydration reminders are issued
 * @param dailyGoalGlasses   glasses of water the person should drink per day
 * @param intervalHours      hours between hydration reminders
 * @param respectSleepWindow whether reminders falling inside the sleep window are suppressed
 */
public record ConfigureHydrationPlanCommand(
        UUID personUnderCareId,
        boolean active,
        Integer dailyGoalGlasses,
        Integer intervalHours,
        boolean respectSleepWindow) {
}
