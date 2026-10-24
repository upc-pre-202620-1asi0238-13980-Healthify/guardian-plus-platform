package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;

import java.time.Instant;

/**
 * Raised when the Fragile Citizen cancels a detected fall within its confirmation window,
 * classifying the alert as a false positive.
 */
public record AlertDismissedEvent(
        AlertId alertId,
        CareRecipientProfileId careRecipientProfileId,
        Instant dismissedAt) {

    public static AlertDismissedEvent from(Alert alert, Instant dismissedAt) {
        return new AlertDismissedEvent(
                alert.getId(), alert.getCareRecipientProfileId(), dismissedAt);
    }
}
