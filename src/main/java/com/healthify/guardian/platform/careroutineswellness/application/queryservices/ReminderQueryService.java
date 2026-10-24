package com.healthify.guardian.platform.careroutineswellness.application.queryservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetReminderAdherenceQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetReminderStatusByIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetRemindersByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.AdherenceSummary;

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
     * Handles retrieval of the reminders scheduled for a person under care.
     *
     * @param query person-under-care-id query, optionally narrowed to a period and a type
     * @return matching reminders, ordered by scheduled time
     */
    List<Reminder> handle(GetRemindersByPersonUnderCareIdQuery query);

    /**
     * Handles the measurement of a person under care's adherence over a period.
     *
     * @param query person, reminder type and period
     * @return the adherence summary, day by day
     */
    AdherenceSummary handle(GetReminderAdherenceQuery query);
}
