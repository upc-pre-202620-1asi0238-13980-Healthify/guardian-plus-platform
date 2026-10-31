package com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications;

import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.AlertNotification;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;

/**
 * Sends notifications through the provider of one specific channel.
 */
public interface ChannelNotificationAdapter {

    /** The channel this adapter serves. */
    NotificationChannel channel();

    /**
     * Hands a notification to this channel's provider.
     *
     * @param notification the notification to send
     * @return {@code SENT} if the provider accepted it, {@code FAILED} otherwise
     */
    DeliveryStatus send(AlertNotification notification);
}
