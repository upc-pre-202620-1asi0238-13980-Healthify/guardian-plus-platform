package com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.HydrationPlan;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelActiveRemindersByTypeCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.IssueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.MarkReminderAsMissedCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ReissueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleNextReminderOccurrenceCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.SleepWindow;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.HydrationPlanRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.MedicationStockRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Application service that executes reminder commands.
 */
@Service
public class ReminderCommandServiceImpl implements ReminderCommandService {

    private final ReminderRepository reminderRepository;
    private final MedicationStockRepository medicationStockRepository;
    private final HydrationPlanRepository hydrationPlanRepository;
    private final SleepWindow defaultSleepWindow;
    private final ZoneId zone;

    public ReminderCommandServiceImpl(
            ReminderRepository reminderRepository,
            MedicationStockRepository medicationStockRepository,
            HydrationPlanRepository hydrationPlanRepository,
            SleepWindow careRoutinesWellnessDefaultSleepWindow,
            ZoneId careRoutinesWellnessZoneId) {
        this.reminderRepository = reminderRepository;
        this.medicationStockRepository = medicationStockRepository;
        this.hydrationPlanRepository = hydrationPlanRepository;
        this.defaultSleepWindow = careRoutinesWellnessDefaultSleepWindow;
        this.zone = careRoutinesWellnessZoneId;
    }

    @Override
    public Result<Reminder, ApplicationError> handle(ScheduleReminderCommand command) {
        if (command.medicationStockId() != null && !stockBelongsToPerson(command.medicationStockId(), command.personUnderCareId())) {
            return Result.failure(ApplicationError.notFound("MedicationStock", command.medicationStockId().toString()));
        }
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
            // without a hydration plan, hydration reminders respect the sleep window by default
            var respectSleepWindow = reminder.get().getType() != ReminderType.HYDRATION
                    || hydrationPlanRepository.findByPersonUnderCareId(reminder.get().getPersonUnderCareId())
                            .map(HydrationPlan::respectsSleepWindow)
                            .orElse(true);
            reminder.get().issue(Instant.now(), defaultSleepWindow, zone, respectSleepWindow);
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

    @Override
    public Result<Reminder, ApplicationError> handle(MarkReminderAsMissedCommand command) {
        return applyToExistingReminder(command.reminderId(), "mark-reminder-as-missed", Reminder::markAsMissed);
    }

    @Override
    public Result<Void, ApplicationError> handle(ScheduleNextReminderOccurrenceCommand command) {
        var reminder = reminderRepository.findById(new ReminderId(command.reminderId()));
        if (reminder.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Reminder", command.reminderId().toString()));
        }
        reminder.get().nextOccurrence(Instant.now(), zone).ifPresent(reminderRepository::save);
        return Result.success(null);
    }

    @Override
    public Result<Void, ApplicationError> handle(CancelActiveRemindersByTypeCommand command) {
        reminderRepository.findActiveByPersonUnderCareIdAndType(new PersonUnderCareId(command.personUnderCareId()), command.type())
                .forEach(reminder -> {
                    reminder.cancel();
                    reminderRepository.save(reminder);
                });
        return Result.success(null);
    }

    private boolean stockBelongsToPerson(UUID medicationStockId, UUID personUnderCareId) {
        return medicationStockRepository.findById(new MedicationStockId(medicationStockId))
                .filter(stock -> stock.getPersonUnderCareId().value().equals(personUnderCareId))
                .isPresent();
    }

    private Result<Reminder, ApplicationError> applyToExistingReminder(
            UUID reminderId, String operation, Consumer<Reminder> transition) {
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
