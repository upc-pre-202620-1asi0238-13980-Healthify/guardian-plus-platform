package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelActiveRemindersByTypeCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.HydrationPlanConfiguredEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RecurrenceFrequency;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Translates a hydration plan's schedule into reminders: whenever the plan is switched on or off or its
 * interval changes, the pending hydration reminders are cancelled and, if the plan is active, a new
 * recurring hydration reminder is started one interval from now.
 *
 * <p>Never lets a failure propagate back to the publisher: the plan stays saved.</p>
 */
@Slf4j
@Service
public class HydrationPlanConfiguredEventHandler {

    private final ReminderCommandService reminderCommandService;
    private final String hydrationReminderTitle;

    public HydrationPlanConfiguredEventHandler(
            ReminderCommandService reminderCommandService,
            @Value("${care-routines-wellness.hydration.reminder-title:Hidratación}") String hydrationReminderTitle) {
        this.reminderCommandService = reminderCommandService;
        this.hydrationReminderTitle = hydrationReminderTitle;
    }

    @EventListener
    public void on(HydrationPlanConfiguredEvent event) {
        if (!event.scheduleChanged()) {
            return;
        }
        var personUnderCareId = event.personUnderCareId().value();
        try {
            reminderCommandService.handle(new CancelActiveRemindersByTypeCommand(personUnderCareId, ReminderType.HYDRATION));
            if (!event.active()) {
                return;
            }

            var firstReminderAt = Instant.now().plus(Duration.ofHours(event.intervalHours()));
            var result = reminderCommandService.handle(new ScheduleReminderCommand(
                    personUnderCareId, ReminderType.HYDRATION, firstReminderAt, hydrationReminderTitle,
                    null, null, null, null, 0, null, RecurrenceFrequency.HOURLY, null, event.intervalHours()));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Hydration reminders for {} could not be scheduled: {}", personUnderCareId, failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure replacing the hydration reminders of {}", personUnderCareId, e);
        }
    }
}
