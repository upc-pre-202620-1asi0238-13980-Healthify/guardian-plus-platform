package com.healthify.guardian.platform.emergencyalerting.infrastructure.notifications;

import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.AlertNotification;
import com.healthify.guardian.platform.emergencyalerting.application.outboundservices.NotificationDispatcher;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DeliveryStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * {@link NotificationDispatcher} that routes every notification to the adapter of its channel.
 */
@Slf4j
@Component
public class RoutingNotificationDispatcher implements NotificationDispatcher {

    private final Map<NotificationChannel, ChannelNotificationAdapter> adapters =
            new EnumMap<>(NotificationChannel.class);

    public RoutingNotificationDispatcher(List<ChannelNotificationAdapter> channelAdapters) {
        channelAdapters.forEach(adapter -> adapters.put(adapter.channel(), adapter));
    }

    @Override
    public DeliveryStatus send(AlertNotification notification) {
        var adapter = adapters.get(notification.channel());
        if (adapter == null) {
            log.warn("No adapter configured for channel {}", notification.channel());
            return DeliveryStatus.FAILED;
        }
        return adapter.send(notification);
    }
}
