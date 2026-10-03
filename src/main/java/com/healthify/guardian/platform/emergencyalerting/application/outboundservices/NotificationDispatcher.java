package com.healthify.guardian.platform.emergencyalerting.application.outboundservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;

/**
 * Outbound port through which this context actually sends notifications, regardless of the
 * provider behind each channel.
 */
public interface NotificationDispatcher {

    /**
     * Hands a notification to the provider of its channel.
     *
     * @param notification the notification to send
     * @return {@code SENT} if the provider accepted it, {@code FAILED} otherwise; later outcomes
     *         (e.g. {@code DELIVERED}) arrive through the provider's delivery webhook
     */
    DeliveryStatus send(AlertNotification notification);
}
