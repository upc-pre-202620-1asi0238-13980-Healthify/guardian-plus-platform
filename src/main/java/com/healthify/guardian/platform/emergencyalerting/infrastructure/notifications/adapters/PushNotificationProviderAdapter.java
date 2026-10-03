package com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.adapters;

import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.AlertNotification;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.ChannelNotificationAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * {@code PUSH} channel.
 *
 * <p><b>Simulated:</b> the Firebase Cloud Messaging integration is pending (decision D2 of the
 * plan), so this adapter only logs the notification. It still fails when the member registered no
 * device token, exactly as the real provider would.</p>
 */
@Slf4j
@Component
public class PushNotificationProviderAdapter implements ChannelNotificationAdapter {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public DeliveryStatus send(AlertNotification notification) {
        if (notification.address() == null) {
            log.warn("PUSH notification of alert {} to {} failed: no device token registered",
                    notification.alertId().value(), notification.recipientUserId().value());
            return DeliveryStatus.FAILED;
        }
        log.info("[simulated PUSH] {} {} {} alert {} to {} (audible={})",
                notification.purpose(), notification.severity(), notification.sourceType(),
                notification.alertId().value(), notification.recipientUserId().value(), notification.audible());
        return DeliveryStatus.SENT;
    }
}
