package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertDelivery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertResponse;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.RecipientLevel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertDeliveryResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertResponseResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertSummaryResource;

import java.util.UUID;

/**
 * Assembler that converts an {@link Alert} domain aggregate into its full and summary resources.
 */
public final class AlertResourceFromEntityAssembler {

    private AlertResourceFromEntityAssembler() {
    }

    public static AlertResource toResourceFromEntity(Alert alert) {
        return new AlertResource(
                alert.getId().value(),
                alert.getCareRecipientProfileId().value(),
                alert.getSource().sourceType().name(),
                alert.getSource().sourceReferenceId(),
                alert.getSeverity().name(),
                alert.getStatus().name(),
                levelName(alert.currentRecipientLevel()),
                alert.getTriggeredAt(),
                alert.getConfirmedAt(),
                alert.getLastDispatchedAt(),
                alert.getAcknowledgedAt(),
                userIdValue(alert.getAcknowledgedByUserId()),
                alert.getResolvedAt(),
                alert.getDeliveries().stream().map(AlertResourceFromEntityAssembler::toResourceFromEntity).toList(),
                alert.getResponses().stream().map(AlertResourceFromEntityAssembler::toResourceFromEntity).toList());
    }

    public static AlertSummaryResource toSummaryResourceFromEntity(Alert alert) {
        return new AlertSummaryResource(
                alert.getId().value(),
                alert.getCareRecipientProfileId().value(),
                alert.getSource().sourceType().name(),
                alert.getSeverity().name(),
                alert.getStatus().name(),
                levelName(alert.currentRecipientLevel()),
                alert.getTriggeredAt(),
                alert.getAcknowledgedAt());
    }

    private static AlertDeliveryResource toResourceFromEntity(AlertDelivery delivery) {
        return new AlertDeliveryResource(
                delivery.getId().value(),
                delivery.getRecipientUserId().value(),
                delivery.getRecipientLevel().name(),
                delivery.getChannel().name(),
                delivery.getDeliveryStatus().name(),
                delivery.getSentAt(),
                delivery.getDeliveredAt());
    }

    private static AlertResponseResource toResourceFromEntity(AlertResponse response) {
        return new AlertResponseResource(
                response.getId().value(),
                response.getResponderUserId().value(),
                response.getResponseStatus().name(),
                response.getClaimedAt(),
                response.getCompletedAt(),
                response.getNotes());
    }

    private static String levelName(RecipientLevel level) {
        return level == null ? null : level.name();
    }

    private static UUID userIdValue(UserId userId) {
        return userId == null ? null : userId.value();
    }
}
