package com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.adapters;

import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.AlertNotification;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.ChannelNotificationAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * {@code IN_APP} channel. Nothing is pushed: the mobile app retrieves in-app alerts through the
 * pending alerts query, so the delivery is considered sent as soon as it is available there.
 */
@Slf4j
@Component
public class InAppNotificationAdapter implements ChannelNotificationAdapter {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public DeliveryStatus send(AlertNotification notification) {
        log.debug("In-app {} notification of alert {} available to {}",
                notification.purpose(), notification.alertId().value(), notification.recipientUserId().value());
        return DeliveryStatus.SENT;
    }
}
