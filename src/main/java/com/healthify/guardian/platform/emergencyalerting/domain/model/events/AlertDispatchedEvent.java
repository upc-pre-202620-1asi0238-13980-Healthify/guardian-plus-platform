package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertDelivery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertDeliveryId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.RecipientLevel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;

import java.time.Instant;
import java.util.List;

/**
 * Raised when the deliveries of one recipient level are generated for an alert.
 *
 * <p>Handled to send each delivery through its notification channel.</p>
 */
public record AlertDispatchedEvent(
        AlertId alertId,
        CareRecipientProfileId careRecipientProfileId,
        AlertSourceType sourceType,
        Severity severity,
        RecipientLevel recipientLevel,
        List<AlertDeliveryId> deliveryIds,
        boolean initialDispatch,
        Instant dispatchedAt) {

    public static AlertDispatchedEvent from(
            Alert alert, RecipientLevel recipientLevel, List<AlertDelivery> deliveries, boolean initialDispatch) {
        return new AlertDispatchedEvent(
                alert.getId(), alert.getCareRecipientProfileId(), alert.getSource().sourceType(),
                alert.getSeverity(), recipientLevel, deliveries.stream().map(AlertDelivery::getId).toList(),
                initialDispatch, alert.getLastDispatchedAt());
    }
}
