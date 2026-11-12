package com.healthify.guardian.platform.emergencyalerting.application.outboundservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;

/**
 * Outbound port through which this context sends the preventive push notifications of care routines (e.g. a
 * medical appointment coming up), regardless of the push provider behind it.
 */
public interface ReminderNotificationDispatcher {

    /**
     * Hands a reminder notification to the push provider.
     *
     * @param notification the notification to send
     * @return {@code SENT} if the provider accepted it, {@code FAILED} otherwise
     */
    DeliveryStatus send(ReminderNotification notification);
}
