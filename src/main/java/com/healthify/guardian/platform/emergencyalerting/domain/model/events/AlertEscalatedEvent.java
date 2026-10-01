package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

import java.time.Instant;

/**
 * Raised when an unacknowledged alert advances to its secondary emergency contacts.
 */
public record AlertEscalatedEvent(
        AlertId alertId,
        CareRecipientProfileId careRecipientProfileId,
        Instant escalatedAt) {

    public static AlertEscalatedEvent from(Alert alert) {
        return new AlertEscalatedEvent(
                alert.getId(), alert.getCareRecipientProfileId(), alert.getLastDispatchedAt());
    }
}
