package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSource;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;

import java.time.Instant;

/**
 * Raised when an alert is confirmed: its fall confirmation window expired without a
 * cancellation, or its source does not require one.
 */
public record AlertConfirmedEvent(
        AlertId alertId,
        CareRecipientProfileId careRecipientProfileId,
        AlertSource source,
        Severity severity,
        Instant confirmedAt) {

    public static AlertConfirmedEvent from(Alert alert) {
        return new AlertConfirmedEvent(
                alert.getId(), alert.getCareRecipientProfileId(), alert.getSource(),
                alert.getSeverity(), alert.getConfirmedAt());
    }
}
