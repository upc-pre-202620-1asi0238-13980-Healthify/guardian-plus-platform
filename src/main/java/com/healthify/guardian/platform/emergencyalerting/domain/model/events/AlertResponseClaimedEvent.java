package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertResponse;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertResponseId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

import java.time.Instant;

/**
 * Raised when a recipient declares they will take charge of the response, so the rest of the
 * Care Circle can be told.
 */
public record AlertResponseClaimedEvent(
        AlertId alertId,
        AlertResponseId responseId,
        UserId responderUserId,
        Instant claimedAt) {

    public static AlertResponseClaimedEvent from(Alert alert, AlertResponse response) {
        return new AlertResponseClaimedEvent(
                alert.getId(), response.getId(), response.getResponderUserId(), response.getClaimedAt());
    }
}
