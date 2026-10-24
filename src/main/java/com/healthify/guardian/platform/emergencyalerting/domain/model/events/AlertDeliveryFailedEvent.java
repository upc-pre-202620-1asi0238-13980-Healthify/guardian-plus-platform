package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertDelivery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertDeliveryId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.NotificationChannel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

import java.time.Instant;

/**
 * Raised when the notification provider reports that a delivery could not be completed.
 */
public record AlertDeliveryFailedEvent(
        AlertId alertId,
        AlertDeliveryId deliveryId,
        UserId recipientUserId,
        NotificationChannel channel,
        Severity severity,
        Instant failedAt) {

    public static AlertDeliveryFailedEvent from(Alert alert, AlertDelivery delivery, Instant failedAt) {
        return new AlertDeliveryFailedEvent(
                alert.getId(), delivery.getId(), delivery.getRecipientUserId(),
                delivery.getChannel(), alert.getSeverity(), failedAt);
    }
}
