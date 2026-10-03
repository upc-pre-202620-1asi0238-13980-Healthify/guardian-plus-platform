package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;

/**
 * Query to list every reminder scheduled for a person under care.
 *
 * @param personUnderCareId the person whose reminders are requested
 */
public record GetRemindersByPersonUnderCareIdQuery(PersonUnderCareId personUnderCareId) {
}
