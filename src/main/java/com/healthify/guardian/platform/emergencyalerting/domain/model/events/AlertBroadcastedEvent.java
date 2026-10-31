package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

import java.time.Instant;

/**
 * Raised when an alert reaches every active emergency contact, either right away or as the
 * last resort of the escalation chain.
 */
public record AlertBroadcastedEvent(
        AlertId alertId,
        CareRecipientProfileId careRecipientProfileId,
        Instant broadcastedAt) {

    public static AlertBroadcastedEvent from(Alert alert) {
        return new AlertBroadcastedEvent(
                alert.getId(), alert.getCareRecipientProfileId(), alert.getLastDispatchedAt());
    }
}
