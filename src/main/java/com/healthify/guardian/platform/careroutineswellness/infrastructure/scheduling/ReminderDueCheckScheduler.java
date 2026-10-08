package com.healthify.guardian.platform.careroutineswellness.infrastructure.scheduling;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.IssueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Implements the <b>Reminder Issuance Policy</b>: periodically issues every reminder whose notification
 * time has been reached. Whether a due reminder is issued or suppressed (hydration during the sleep window)
 * is decided by the {@code Reminder} aggregate itself.
 */
@Slf4j
@Component
public class ReminderDueCheckScheduler {

    private final ReminderRepository reminderRepository;
    private final ReminderCommandService reminderCommandService;

    public ReminderDueCheckScheduler(ReminderRepository reminderRepository, ReminderCommandService reminderCommandService) {
        this.reminderRepository = reminderRepository;
        this.reminderCommandService = reminderCommandService;
    }

    @Scheduled(fixedDelayString = "${care-routines-wellness.reminder.scheduler.due-check-delay-ms:30000}")
    public void issueDueReminders() {
        reminderRepository.findDueForIssuance(Instant.now()).forEach(reminder -> {
            var reminderId = reminder.getId().value();
            try {
                var result = reminderCommandService.handle(new IssueReminderCommand(reminderId));
                if (result instanceof Result.Failure<?, ?> failure) {
                    log.warn("Reminder {} could not be issued: {}", reminderId, failure.error());
                }
            } catch (RuntimeException e) {
                log.error("Unexpected failure issuing reminder {}", reminderId, e);
            }
        });
    }
}
