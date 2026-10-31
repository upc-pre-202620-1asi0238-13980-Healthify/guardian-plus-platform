package com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects;

/**
 * A recipient and channel an alert must be delivered to, as resolved by {@code EscalationPolicy}.
 *
 * @param recipientUserId the Care Circle member to notify
 * @param channel         the channel to notify them through
 */
public record DeliveryTarget(UserId recipientUserId, NotificationChannel channel) {

    private static final String INVALID_MESSAGE_KEY = "delivery-target.invalid";

    public DeliveryTarget {
        if (recipientUserId == null || channel == null) {
            throw new IllegalArgumentException(INVALID_MESSAGE_KEY);
        }
    }
}
