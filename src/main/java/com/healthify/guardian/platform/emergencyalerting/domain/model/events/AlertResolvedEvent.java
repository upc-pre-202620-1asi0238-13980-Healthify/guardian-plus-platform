package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

import java.time.Instant;

/**
 * Raised when an alert is definitively closed after its incident is closed.
 */
public record AlertResolvedEvent(
        AlertId alertId,
        CareRecipientProfileId careRecipientProfileId,
        Instant resolvedAt) {

    public static AlertResolvedEvent from(Alert alert) {
        return new AlertResolvedEvent(
                alert.getId(), alert.getCareRecipientProfileId(), alert.getResolvedAt());
    }
}
