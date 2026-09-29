package com.healthify.guardian.platform.careroutineswellness.infrastructure.scheduling;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.IssueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Periodically issues every reminder whose scheduled time has been reached.
 */
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
        var currentTime = Instant.now();
        reminderRepository.findDueForIssuance(currentTime)
                .forEach(reminder -> reminderCommandService.handle(new IssueReminderCommand(reminder.getId().value())));
    }
}
