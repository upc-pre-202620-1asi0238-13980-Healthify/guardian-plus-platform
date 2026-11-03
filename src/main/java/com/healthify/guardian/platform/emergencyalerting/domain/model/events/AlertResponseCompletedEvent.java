package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertResponse;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertResponseId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

import java.time.Instant;

/**
 * Raised when the responder records the outcome of their intervention.
 */
public record AlertResponseCompletedEvent(
        AlertId alertId,
        AlertResponseId responseId,
        UserId responderUserId,
        Instant completedAt) {

    public static AlertResponseCompletedEvent from(Alert alert, AlertResponse response) {
        return new AlertResponseCompletedEvent(
                alert.getId(), response.getId(), response.getResponderUserId(), response.getCompletedAt());
    }
}
