package com.healthify.guardian.platform.careroutineswellness.domain.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;

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
     * Retrieves every reminder scheduled for a person under care.
     *
     * @param personUnderCareId the person whose reminders are requested
     * @return the matching reminders, most relevant first
     */
    List<Reminder> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);

    /**
     * Retrieves every scheduled reminder whose scheduled time has already been reached,
     * for {@code ReminderDueCheckScheduler} to issue.
     *
     * @param currentTime the time to compare each reminder's scheduled time against
     * @return the reminders due for issuance
     */
    List<Reminder> findDueForIssuance(Instant currentTime);

    /**
     * Retrieves every issued medication reminder that {@code ReminderReissuePolicy} determines
     * is overdue for reissuing, for {@code ReminderReissueScheduler} to reissue.
     *
     * @param currentTime the time to evaluate the reissue tolerance against
     * @return the reminders overdue for reissue
     */
    List<Reminder> findOverdueForReissue(Instant currentTime);

    /**
     * Persists a reminder (create or update) and publishes its registered domain events.
     *
     * @param reminder the reminder to save
     * @return the saved reminder
     */
    Reminder save(Reminder reminder);
}
