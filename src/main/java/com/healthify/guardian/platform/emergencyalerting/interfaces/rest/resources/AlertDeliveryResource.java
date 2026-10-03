package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload representing one delivery of an alert.
 *
 * @param id              the delivery identifier
 * @param recipientUserId the notified Care Circle member
 * @param recipientLevel  the escalation level the delivery was sent at
 * @param channel         the notification channel
 * @param deliveryStatus  the provider-reported status
 * @param sentAt          when the provider accepted it
 * @param deliveredAt     when the provider confirmed it reached the device
 */
public record AlertDeliveryResource(
        UUID id,
        UUID recipientUserId,
        String recipientLevel,
        String channel,
        String deliveryStatus,
        Instant sentAt,
        Instant deliveredAt
) {
}
