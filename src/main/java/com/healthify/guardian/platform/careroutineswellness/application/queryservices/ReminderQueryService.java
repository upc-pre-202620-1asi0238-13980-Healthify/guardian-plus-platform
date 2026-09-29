package com.healthify.guardian.platform.careroutineswellness.application.queryservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetRemindersByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetReminderStatusByIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for reminder read queries.
 */
public interface ReminderQueryService {

    /**
     * Handles retrieval of a reminder by its unique identifier.
     *
     * @param query reminder-id query
     * @return matching reminder, if found
     */
    Optional<Reminder> handle(GetReminderStatusByIdQuery query);

    /**
     * Handles retrieval of every reminder scheduled for a person under care.
     *
     * @param query person-under-care-id query
     * @return matching reminders
     */
    List<Reminder> handle(GetRemindersByPersonUnderCareIdQuery query);
}
