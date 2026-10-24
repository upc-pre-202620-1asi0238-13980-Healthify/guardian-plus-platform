package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

/**
 * Query to list the notification channels configured by a Care Circle member.
 *
 * @param userId the Care Circle member whose channels are requested
 */
public record GetAlertChannelSettingsByUserIdQuery(UserId userId) {
}
