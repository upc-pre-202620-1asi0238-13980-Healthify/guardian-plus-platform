package com.healthify.guardian.platform.careroutineswellness.application.commandservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.IssueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ReissueReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ScheduleReminderCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code Reminder} aggregate.
 */
public interface ReminderCommandService {

    /**
     * Schedules a new reminder.
     *
     * @param command the scheduling data
     * @return the newly scheduled reminder or an application error
     */
    Result<Reminder, ApplicationError> handle(ScheduleReminderCommand command);

    /**
     * Issues a reminder that has reached its scheduled time, applying
     * {@code ReminderIssuancePolicy}'s outcome.
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
}
