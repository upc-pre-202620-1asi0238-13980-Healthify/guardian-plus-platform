package com.healthify.guardian.platform.careroutineswellness.domain.model.queries;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;

/**
 * Query to retrieve a single reminder by its identifier.
 *
 * @param reminderId the reminder identifier
 */
public record GetReminderStatusByIdQuery(ReminderId reminderId) {
}
