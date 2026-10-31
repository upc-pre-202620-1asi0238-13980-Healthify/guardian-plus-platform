package com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Response payload with a person under care's hydration plan and today's progress.
 *
 * @param id                    the hydration plan's unique identifier
 * @param personUnderCareId     the person the plan belongs to
 * @param active                whether periodic hydration reminders are issued ("Recordatorios activos")
 * @param dailyGoalGlasses      glasses of water per day ("Meta diaria de agua")
 * @param intervalHours         hours between hydration reminders ("Intervalo")
 * @param respectSleepWindow    whether reminders inside the sleep window are suppressed ("Respetar horas de sueño")
 * @param sleepWindowStart      start of the sleep window
 * @param sleepWindowEnd        end of the sleep window
 * @param glassesConsumedToday  hydration reminders confirmed today
 * @param remainingGlassesToday glasses still missing to reach today's goal
 * @param nextReminderAt        when the next hydration reminder is due, or null when none is pending
 */
public record HydrationPlanResource(
        UUID id,
        UUID personUnderCareId,
        boolean active,
        Integer dailyGoalGlasses,
        Integer intervalHours,
        boolean respectSleepWindow,
        LocalTime sleepWindowStart,
        LocalTime sleepWindowEnd,
        int glassesConsumedToday,
        int remainingGlassesToday,
        Instant nextReminderAt
) {
}
