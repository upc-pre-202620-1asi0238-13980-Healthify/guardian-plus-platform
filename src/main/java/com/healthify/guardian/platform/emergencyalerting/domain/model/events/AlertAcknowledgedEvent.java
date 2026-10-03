package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

import java.time.Instant;

/**
 * Raised when a recipient acknowledges an alert, which stops its escalation.
 */
public record AlertAcknowledgedEvent(
        AlertId alertId,
        CareRecipientProfileId careRecipientProfileId,
        UserId acknowledgedByUserId,
        Instant acknowledgedAt) {

    public static AlertAcknowledgedEvent from(Alert alert) {
        return new AlertAcknowledgedEvent(
                alert.getId(), alert.getCareRecipientProfileId(), alert.getAcknowledgedByUserId(),
                alert.getAcknowledgedAt());
    }
}
