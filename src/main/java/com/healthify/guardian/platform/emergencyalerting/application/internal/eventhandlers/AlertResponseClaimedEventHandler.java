package com.healthify.guardian.platform.emergencyalerting.application.internal.eventhandlers;

import com.healthify.guardian.platform.emergencyalerting.application.internal.outboundservices.AlertNotificationSender;
import com.healthify.guardian.platform.emergencyalerting.domain.model.events.AlertResponseClaimedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Tells the rest of the Care Circle notified of an alert that a member has already taken charge of
 * it, so nobody else rushes over needlessly (US25, scenario 2).
 */
@Service
public class AlertResponseClaimedEventHandler {

    private final AlertNotificationSender alertNotificationSender;

    public AlertResponseClaimedEventHandler(AlertNotificationSender alertNotificationSender) {
        this.alertNotificationSender = alertNotificationSender;
    }

    @EventListener
    public void on(AlertResponseClaimedEvent event) {
        alertNotificationSender.sendResponseClaimed(event.alertId(), event.responderUserId());
    }
}
