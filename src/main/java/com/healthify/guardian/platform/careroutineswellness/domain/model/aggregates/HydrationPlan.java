package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfigureHydrationPlanCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.HydrationPlanConfiguredEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.HydrationPlanId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

/**
 * Aggregate root holding how a person under care should be reminded to drink water: the daily goal,
 * the interval between periodic hydration reminders and whether those reminders respect the sleep window.
 *
 * <p>Exactly one plan exists per person under care. The plan does not issue reminders itself: whenever its
 * schedule changes it raises {@link HydrationPlanConfiguredEvent}, and the application layer replaces the
 * pending recurring {@code HYDRATION} reminder accordingly.</p>
 */
@Getter
public class HydrationPlan extends AbstractDomainAggregateRoot<HydrationPlan> {

    private static final int MIN_DAILY_GOAL_GLASSES = 1;
    private static final int MAX_DAILY_GOAL_GLASSES = 20;
    private static final int MIN_INTERVAL_HOURS = 1;
    private static final int MAX_INTERVAL_HOURS = 12;
    private static final String DAILY_GOAL_INVALID_MESSAGE_KEY = "hydration-plan.daily-goal-glasses.invalid";
    private static final String INTERVAL_INVALID_MESSAGE_KEY = "hydration-plan.interval-hours.invalid";

    private HydrationPlanId id;
    private PersonUnderCareId personUnderCareId;
    private Boolean active;
    private Integer dailyGoalGlasses;
    private Integer intervalHours;
    private Boolean respectSleepWindow;

    /** Reconstitution constructor, used by the persistence assembler. */
    public HydrationPlan() {
    }

    /** Creates the hydration plan of a person under care from its first configuration. */
    public HydrationPlan(ConfigureHydrationPlanCommand command) {
        this.id = HydrationPlanId.generate();
        this.personUnderCareId = new PersonUnderCareId(command.personUnderCareId());
        validate(command);
        apply(command, true);
    }

    /**
     * Changes the plan. The pending reminder only has to be replaced when the plan is switched on or off or
     * its interval changes; a new daily goal or sleep-window preference does not touch the schedule.
     *
     * @param command the new configuration
     */
    public void configure(ConfigureHydrationPlanCommand command) {
        validate(command);
        var scheduleChanged = active == null || active != command.active()
                || !command.intervalHours().equals(intervalHours);
        apply(command, scheduleChanged);
    }

    private void apply(ConfigureHydrationPlanCommand command, boolean scheduleChanged) {
        this.active = command.active();
        this.dailyGoalGlasses = command.dailyGoalGlasses();
        this.intervalHours = command.intervalHours();
        this.respectSleepWindow = command.respectSleepWindow();
        registerDomainEvent(HydrationPlanConfiguredEvent.from(this, scheduleChanged));
    }

    private static void validate(ConfigureHydrationPlanCommand command) {
        var goal = command.dailyGoalGlasses();
        if (goal == null || goal < MIN_DAILY_GOAL_GLASSES || goal > MAX_DAILY_GOAL_GLASSES) {
            throw new IllegalArgumentException(DAILY_GOAL_INVALID_MESSAGE_KEY);
        }
        var interval = command.intervalHours();
        if (interval == null || interval < MIN_INTERVAL_HOURS || interval > MAX_INTERVAL_HOURS) {
            throw new IllegalArgumentException(INTERVAL_INVALID_MESSAGE_KEY);
        }
    }

    /** True when reminders falling inside the sleep window must be suppressed. */
    public boolean respectsSleepWindow() {
        return !Boolean.FALSE.equals(respectSleepWindow);
    }

    /** True while periodic hydration reminders are being issued. */
    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(HydrationPlanId id) {
        this.id = id;
    }

    public void setPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        this.personUnderCareId = personUnderCareId;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setDailyGoalGlasses(Integer dailyGoalGlasses) {
        this.dailyGoalGlasses = dailyGoalGlasses;
    }

    public void setIntervalHours(Integer intervalHours) {
        this.intervalHours = intervalHours;
    }

    public void setRespectSleepWindow(Boolean respectSleepWindow) {
        this.respectSleepWindow = respectSleepWindow;
    }
}
