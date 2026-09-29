package com.healthify.guardian.platform.careroutineswellness.infrastructure.scheduling;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ReissueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Periodically reissues every medication reminder that {@code ReminderReissuePolicy} determines
 * is overdue for confirmation.
 */
@Component
public class ReminderReissueScheduler {

    private final ReminderRepository reminderRepository;
    private final ReminderCommandService reminderCommandService;

    public ReminderReissueScheduler(ReminderRepository reminderRepository, ReminderCommandService reminderCommandService) {
        this.reminderRepository = reminderRepository;
        this.reminderCommandService = reminderCommandService;
    }

    @Scheduled(fixedDelayString = "${care-routines-wellness.reminder.scheduler.reissue-check-delay-ms:60000}")
    public void reissueOverdueReminders() {
        var currentTime = Instant.now();
        reminderRepository.findOverdueForReissue(currentTime)
                .forEach(reminder -> reminderCommandService.handle(new ReissueReminderCommand(reminder.getId().value())));
    }
}
