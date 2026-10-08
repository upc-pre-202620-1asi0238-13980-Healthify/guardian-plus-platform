package com.healthify.guardian.platform.careroutineswellness.application.commandservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelActiveRemindersByTypeCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.IssueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.MarkReminderAsMissedCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ReissueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleNextReminderOccurrenceCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code Reminder} aggregate.
 */
public interface ReminderCommandService {

    /**
     * Schedules a new reminder (the first occurrence of its series).
     *
     * @param command the scheduling data
     * @return the newly scheduled reminder or an application error
     */
    Result<Reminder, ApplicationError> handle(ScheduleReminderCommand command);

    /**
     * Issues a reminder that has reached its notification time, or suppresses it when the
     * {@code Reminder} aggregate's sleep-window rule applies.
     *
     * @param command the reminder to issue
     * @return the updated reminder or an application error
     */
    Result<Reminder, ApplicationError> handle(IssueReminderCommand command);

    /**
     * Confirms an issued (or reissued) reminder.
     *
     * @param command the reminder to confirm
     * @return the updated reminder or an application error
     */
    Result<Reminder, ApplicationError> handle(ConfirmReminderCommand command);

    /**
     * Cancels a reminder that has not been confirmed yet.
     *
     * @param command the reminder to cancel
     * @return the updated reminder or an application error
     */
    Result<Reminder, ApplicationError> handle(CancelReminderCommand command);

    /**
     * Reissues a medication reminder overdue for confirmation.
     *
     * @param command the reminder to reissue
     * @return the updated reminder or an application error
     */
    Result<Reminder, ApplicationError> handle(ReissueReminderCommand command);

    /**
     * Closes an unconfirmed reminder as missed, once the family member has reviewed the omission.
     *
     * @param command the reminder to close
     * @return the updated reminder or an application error
     */
    Result<Reminder, ApplicationError> handle(MarkReminderAsMissedCommand command);

    /**
     * Schedules the next occurrence of a recurring reminder's series. Does nothing for one-off reminders.
     *
     * @param command the occurrence that has just left the {@code SCHEDULED} status
     * @return nothing on success, or an application error
     */
    Result<Void, ApplicationError> handle(ScheduleNextReminderOccurrenceCommand command);

    /**
     * Cancels every still-active reminder of one type for a person under care.
     *
     * @param command the person and the reminder type
     * @return nothing on success, or an application error
     */
    Result<Void, ApplicationError> handle(CancelActiveRemindersByTypeCommand command);
}
