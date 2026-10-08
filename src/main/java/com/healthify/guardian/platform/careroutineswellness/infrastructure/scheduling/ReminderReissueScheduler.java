package com.healthify.guardian.platform.careroutineswellness.infrastructure.scheduling;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ReissueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReissueTolerance;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import com.healthify.guardian.platform.shared.application.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Implements the <b>Reminder Reissue Policy</b> ("whenever a medication reminder is not confirmed within
 * 10 minutes, reissue it"): periodically reissues every reminder the {@code Reminder} aggregate reports as
 * overdue for the configured {@link ReissueTolerance}.
 */
@Slf4j
@Component
public class ReminderReissueScheduler {

    private final ReminderRepository reminderRepository;
    private final ReminderCommandService reminderCommandService;
    private final ReissueTolerance reissueTolerance;

    public ReminderReissueScheduler(
            ReminderRepository reminderRepository,
            ReminderCommandService reminderCommandService,
            ReissueTolerance careRoutinesWellnessReissueTolerance) {
        this.reminderRepository = reminderRepository;
        this.reminderCommandService = reminderCommandService;
        this.reissueTolerance = careRoutinesWellnessReissueTolerance;
    }

    @Scheduled(fixedDelayString = "${care-routines-wellness.reminder.scheduler.reissue-check-delay-ms:60000}")
    public void reissueOverdueReminders() {
        var currentTime = Instant.now();
        reminderRepository.findAwaitingConfirmation().stream()
                .filter(reminder -> reminder.isOverdueForReissue(currentTime, reissueTolerance))
                .forEach(reminder -> {
                    var reminderId = reminder.getId().value();
                    try {
                        var result = reminderCommandService.handle(new ReissueReminderCommand(reminderId));
                        if (result instanceof Result.Failure<?, ?> failure) {
                            log.warn("Reminder {} could not be reissued: {}", reminderId, failure.error());
                        }
                    } catch (RuntimeException e) {
                        log.error("Unexpected failure reissuing reminder {}", reminderId, e);
                    }
                });
    }
}
