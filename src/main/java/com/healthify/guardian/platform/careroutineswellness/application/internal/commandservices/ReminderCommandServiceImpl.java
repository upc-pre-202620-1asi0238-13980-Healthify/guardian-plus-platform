package com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.IssueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ReissueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.services.ReminderIssuancePolicy;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Application service that executes reminder commands.
 */
@Service
public class ReminderCommandServiceImpl implements ReminderCommandService {

    private final ReminderRepository reminderRepository;
    private final ReminderIssuancePolicy reminderIssuancePolicy;
    private final SleepWindow defaultSleepWindow;

    public ReminderCommandServiceImpl(
            ReminderRepository reminderRepository,
            ReminderIssuancePolicy reminderIssuancePolicy,
            SleepWindow careRoutinesWellnessDefaultSleepWindow) {
        this.reminderRepository = reminderRepository;
        this.reminderIssuancePolicy = reminderIssuancePolicy;
        this.defaultSleepWindow = careRoutinesWellnessDefaultSleepWindow;
    }

    @Override
    public Result<Reminder, ApplicationError> handle(ScheduleReminderCommand command) {
        try {
            var reminder = new Reminder(command);
            return Result.success(reminderRepository.save(reminder));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("schedule-reminder", resolve(e)));
        }
    }

    @Override
    public Result<Reminder, ApplicationError> handle(IssueReminderCommand command) {
        var reminder = reminderRepository.findById(new ReminderId(command.reminderId()));
        if (reminder.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Reminder", command.reminderId().toString()));
        }

        try {
            var currentTime = Instant.now();
            var outcome = reminderIssuancePolicy.determineIssuanceOutcome(reminder.get(), currentTime, defaultSleepWindow);
            reminder.get().issue(outcome, currentTime);
            return Result.success(reminderRepository.save(reminder.get()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation("issue-reminder", resolve(e)));
        }
    }

    @Override
    public Result<Reminder, ApplicationError> handle(ConfirmReminderCommand command) {
        return applyToExistingReminder(command.reminderId(), "confirm-reminder", Reminder::confirm);
    }

    @Override
    public Result<Reminder, ApplicationError> handle(CancelReminderCommand command) {
        return applyToExistingReminder(command.reminderId(), "cancel-reminder", Reminder::cancel);
    }

    @Override
    public Result<Reminder, ApplicationError> handle(ReissueReminderCommand command) {
        return applyToExistingReminder(command.reminderId(), "reissue-reminder", Reminder::reissue);
    }

    private Result<Reminder, ApplicationError> applyToExistingReminder(
            UUID reminderId, String operation, java.util.function.Consumer<Reminder> transition) {
        var reminder = reminderRepository.findById(new ReminderId(reminderId));
        if (reminder.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Reminder", reminderId.toString()));
        }

        try {
            transition.accept(reminder.get());
            return Result.success(reminderRepository.save(reminder.get()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation(operation, resolve(e)));
        }
    }

    /** Resolves a domain exception whose message is a bundle key into the localized sentence. */
    private static String resolve(RuntimeException e) {
        return MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage());
    }
}
