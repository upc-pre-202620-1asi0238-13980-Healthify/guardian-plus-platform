package com.healthify.guardian.platform.careroutineswellness.application.internal.eventhandlers;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleNextReminderOccurrenceCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.ReminderSuppressedEvent;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Implements the <b>Recurrence Policy</b> for suppressed reminders: a hydration reminder skipped during the
 * sleep window still schedules the next occurrence of its series, so the cycle resumes in the morning.
 *
 * <p>Never lets a failure propagate back to the publisher.</p>
 */
@Slf4j
@Service
public class ReminderSuppressedEventHandler {

    private final ReminderCommandService reminderCommandService;

    public ReminderSuppressedEventHandler(ReminderCommandService reminderCommandService) {
        this.reminderCommandService = reminderCommandService;
    }

    @EventListener
    public void on(ReminderSuppressedEvent event) {
        var reminderId = event.reminderId().value();
        try {
            var result = reminderCommandService.handle(new ScheduleNextReminderOccurrenceCommand(reminderId));
            if (result instanceof Result.Failure<?, ?> failure) {
                log.warn("Next occurrence of reminder {} could not be scheduled: {}", reminderId, failure.error());
            }
        } catch (RuntimeException e) {
            log.error("Unexpected failure scheduling the next occurrence of reminder {}", reminderId, e);
        }
    }
}
