package com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.adapters;

import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.AlertNotification;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications.ChannelNotificationAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * {@code SMS} channel, used as the mandatory backup of critical alerts and broadcasts.
 *
 * <p><b>Simulated:</b> no SMS provider is integrated yet (decision D2 of the plan), so this adapter
 * only logs the message. It still fails when the contact has no phone number, exactly as the real
 * provider would. Phone numbers are masked in the log.</p>
 */
@Slf4j
@Component
public class SmsProviderAdapter implements ChannelNotificationAdapter {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public DeliveryStatus send(AlertNotification notification) {
        if (notification.address() == null) {
            log.warn("SMS notification of alert {} to {} failed: no phone number known",
                    notification.alertId().value(), notification.recipientUserId().value());
            return DeliveryStatus.FAILED;
        }
        log.info("[simulated SMS] {} {} {} alert {} to {}",
                notification.purpose(), notification.severity(), notification.sourceType(),
                notification.alertId().value(), mask(notification.address()));
        return DeliveryStatus.SENT;
    }

    private static String mask(String phoneNumber) {
        if (phoneNumber.length() <= 4) {
            return "****";
        }
        var visible = phoneNumber.length() - 4;
        return "*".repeat(visible) + phoneNumber.substring(visible);
    }
}
