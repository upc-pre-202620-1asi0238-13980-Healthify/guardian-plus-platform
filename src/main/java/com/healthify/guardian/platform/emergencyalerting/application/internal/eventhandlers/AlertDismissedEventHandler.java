package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.application.internal.outboundservices.AlertNotificationSender;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertDismissedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Tells the caregivers that the Fragile Citizen cancelled a detected fall from their wristband within its
 * confirmation window, so they know it was a false alarm (US10, scenario 1).
 */
@Service
public class AlertDismissedEventHandler {

    private final AlertNotificationSender alertNotificationSender;

    public AlertDismissedEventHandler(AlertNotificationSender alertNotificationSender) {
        this.alertNotificationSender = alertNotificationSender;
    }

    @EventListener
    public void on(AlertDismissedEvent event) {
        alertNotificationSender.sendDismissal(event.alertId());
    }
}
