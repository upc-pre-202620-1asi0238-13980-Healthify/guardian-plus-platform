package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;

import java.time.Instant;

/**
 * Query to list the reminders scheduled for a person under care, optionally narrowed to a period and a type.
 *
 * @param personUnderCareId the person whose reminders are requested
 * @param from              earliest scheduled time, inclusive; unbounded when null
 * @param to                latest scheduled time, exclusive; unbounded when null
 * @param type              only reminders of this type; every type when null
 */
public record GetRemindersByPersonUnderCareIdQuery(
        PersonUnderCareId personUnderCareId, Instant from, Instant to, ReminderType type) {

    /** Query for every reminder of a person under care, with no filter. */
    public GetRemindersByPersonUnderCareIdQuery(PersonUnderCareId personUnderCareId) {
        this(personUnderCareId, null, null, null);
    }
}
