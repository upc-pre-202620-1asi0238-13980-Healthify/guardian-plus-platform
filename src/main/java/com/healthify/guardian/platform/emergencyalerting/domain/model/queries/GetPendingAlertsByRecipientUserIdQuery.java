package com.healthify.guardian.platform.emergencyalerting.domain.model.queries;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

/**
 * Query to list the alerts a recipient was notified about and that still await acknowledgement.
 *
 * @param recipientUserId the recipient whose pending alerts are requested
 */
public record GetPendingAlertsByRecipientUserIdQuery(UserId recipientUserId) {
}
