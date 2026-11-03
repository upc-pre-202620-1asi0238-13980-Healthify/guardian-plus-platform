package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfigureHydrationPlanCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.HydrationPlanConfiguredEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HydrationPlanTest {

    private static final UUID PERSON = UUID.randomUUID();

    private static ConfigureHydrationPlanCommand plan(boolean active, int goal, int interval, boolean respectSleep) {
        return new ConfigureHydrationPlanCommand(PERSON, active, goal, interval, respectSleep);
    }

    private static boolean scheduleChanged(HydrationPlan plan) {
        return plan.domainEvents().stream()
                .map(HydrationPlanConfiguredEvent.class::cast)
                .reduce((first, last) -> last)
                .orElseThrow()
                .scheduleChanged();
    }

    @Test
    void creatingThePlanStartsTheSchedule() {
        var hydrationPlan = new HydrationPlan(plan(true, 6, 2, true));

        assertThat(scheduleChanged(hydrationPlan)).isTrue();
    }

    @Test
    void onlySwitchingOrChangingTheIntervalReplacesTheSchedule() {
        var hydrationPlan = new HydrationPlan(plan(true, 6, 2, true));
        hydrationPlan.clearDomainEvents();

        hydrationPlan.configure(plan(true, 8, 2, false));
        assertThat(scheduleChanged(hydrationPlan)).isFalse();

        hydrationPlan.configure(plan(true, 8, 3, false));
        assertThat(scheduleChanged(hydrationPlan)).isTrue();

        hydrationPlan.configure(plan(false, 8, 3, false));
        assertThat(scheduleChanged(hydrationPlan)).isTrue();
    }

    @Test
    void rejectsAGoalOrIntervalOutOfRange() {
        assertThatThrownBy(() -> new HydrationPlan(plan(true, 0, 2, true)))
                .hasMessage("hydration-plan.daily-goal-glasses.invalid");
        assertThatThrownBy(() -> new HydrationPlan(plan(true, 6, 13, true)))
                .hasMessage("hydration-plan.interval-hours.invalid");
    }
}
