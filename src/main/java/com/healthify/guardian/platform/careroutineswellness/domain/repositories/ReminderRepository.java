package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Reminder aggregate repository port.
 */
public interface ReminderRepository {

    /**
     * Retrieves a reminder by its unique identifier.
     *
     * @param id the reminder identifier
     * @return the matching reminder, if found
     */
    Optional<Reminder> findById(ReminderId id);

    /**
     * Retrieves the reminders scheduled for a person under care, optionally narrowed to a period and a type.
     *
     * @param personUnderCareId the person whose reminders are requested
     * @param from              earliest scheduled time, inclusive; unbounded when null
     * @param to                latest scheduled time, exclusive; unbounded when null
     * @param type              only reminders of this type; every type when null
     * @return the matching reminders, ordered by scheduled time
     */
    List<Reminder> findByPersonUnderCareId(PersonUnderCareId personUnderCareId, Instant from, Instant to, ReminderType type);

    /**
     * Retrieves the still-active (scheduled, issued or reissued) reminders of one type for a person under care.
     *
     * @param personUnderCareId the person whose reminders are requested
     * @param type              the reminder type
     * @return the matching reminders, ordered by scheduled time
     */
    List<Reminder> findActiveByPersonUnderCareIdAndType(PersonUnderCareId personUnderCareId, ReminderType type);

    /**
     * Retrieves every scheduled reminder whose notification time has already been reached,
     * for {@code ReminderDueCheckScheduler} to issue.
     *
     * @param currentTime the time to compare each reminder's notification time against
     * @return the reminders due for issuance
     */
    List<Reminder> findDueForIssuance(Instant currentTime);

    /**
     * Retrieves every reminder issued once and still waiting for the person's confirmation, for
     * {@code ReminderReissueScheduler} to decide which ones are overdue.
     *
     * @return the issued reminders awaiting confirmation
     */
    List<Reminder> findAwaitingConfirmation();

    /**
     * Persists a reminder (create or update) and publishes its registered domain events.
     *
     * @param reminder the reminder to save
     * @return the saved reminder
     */
    Reminder save(Reminder reminder);
}
