package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSource;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;

import java.time.Instant;

/**
 * Raised when a risk signal triggers a new alert.
 */
public record AlertTriggeredEvent(
        AlertId alertId,
        CareRecipientProfileId careRecipientProfileId,
        AlertSource source,
        Severity severity,
        AlertStatus status,
        Instant triggeredAt) {

    public static AlertTriggeredEvent from(Alert alert) {
        return new AlertTriggeredEvent(
                alert.getId(), alert.getCareRecipientProfileId(), alert.getSource(),
                alert.getSeverity(), alert.getStatus(), alert.getTriggeredAt());
    }
}
