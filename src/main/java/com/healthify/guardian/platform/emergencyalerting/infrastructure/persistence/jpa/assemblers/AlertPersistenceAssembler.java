package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertDelivery;
import com.healthify.guardian.platform.emergencyalerting.domain.model.entities.AlertResponse;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertDeliveryId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertResponseId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSource;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.embeddables.AlertSourceEmbeddable;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertDeliveryPersistenceEntity;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertResponsePersistenceEntity;

import java.util.ArrayList;

/**
 * Static assembler between the {@link Alert} domain aggregate, including its deliveries and
 * responses, and its persistence entities.
 */
public final class AlertPersistenceAssembler {

    private AlertPersistenceAssembler() {
    }

    public static Alert toDomainFromPersistence(AlertPersistenceEntity entity) {
        if (entity == null) return null;
        var alert = new Alert();
        alert.setId(new AlertId(entity.getId()));
        alert.setCareRecipientProfileId(entity.getCareRecipientProfileId());
        alert.setSource(new AlertSource(entity.getSource().getSourceType(), entity.getSource().getSourceReferenceId()));
        alert.setSeverity(entity.getSeverity());
        alert.setStatus(entity.getStatus());
        alert.setTriggeredAt(entity.getTriggeredAt());
        alert.setConfirmedAt(entity.getConfirmedAt());
        alert.setLastDispatchedAt(entity.getLastDispatchedAt());
        alert.setAcknowledgedAt(entity.getAcknowledgedAt());
        alert.setAcknowledgedByUserId(entity.getAcknowledgedByUserId());
        alert.setResolvedAt(entity.getResolvedAt());
        alert.setVersion(entity.getVersion());
        alert.setDeliveries(entity.getDeliveries().stream()
                .map(AlertPersistenceAssembler::toDomainFromPersistence)
                .toList());
        alert.setResponses(entity.getResponses().stream()
                .map(AlertPersistenceAssembler::toDomainFromPersistence)
                .toList());
        return alert;
    }

    public static AlertPersistenceEntity toPersistenceFromDomain(Alert alert) {
        if (alert == null) return null;
        var entity = new AlertPersistenceEntity();
        entity.setId(alert.getId().value());
        entity.setCareRecipientProfileId(alert.getCareRecipientProfileId());
        entity.setSource(new AlertSourceEmbeddable(
                alert.getSource().sourceType(), alert.getSource().sourceReferenceId()));
        entity.setSeverity(alert.getSeverity());
        entity.setStatus(alert.getStatus());
        entity.setTriggeredAt(alert.getTriggeredAt());
        entity.setConfirmedAt(alert.getConfirmedAt());
        entity.setLastDispatchedAt(alert.getLastDispatchedAt());
        entity.setAcknowledgedAt(alert.getAcknowledgedAt());
        entity.setAcknowledgedByUserId(alert.getAcknowledgedByUserId());
        entity.setResolvedAt(alert.getResolvedAt());
        entity.setVersion(alert.getVersion());

        var deliveries = new ArrayList<AlertDeliveryPersistenceEntity>();
        for (var delivery : alert.getDeliveries()) {
            deliveries.add(toPersistenceFromDomain(delivery, entity));
        }
        entity.setDeliveries(deliveries);

        var responses = new ArrayList<AlertResponsePersistenceEntity>();
        for (var response : alert.getResponses()) {
            responses.add(toPersistenceFromDomain(response, entity));
        }
        entity.setResponses(responses);
        return entity;
    }

    private static AlertDelivery toDomainFromPersistence(AlertDeliveryPersistenceEntity entity) {
        return new AlertDelivery(
                new AlertDeliveryId(entity.getId()),
                entity.getRecipientUserId(),
                entity.getRecipientLevel(),
                entity.getChannel(),
                entity.getDeliveryStatus(),
                entity.getSentAt(),
                entity.getDeliveredAt());
    }

    private static AlertResponse toDomainFromPersistence(AlertResponsePersistenceEntity entity) {
        return new AlertResponse(
                new AlertResponseId(entity.getId()),
                entity.getResponderUserId(),
                entity.getResponseStatus(),
                entity.getClaimedAt(),
                entity.getCompletedAt(),
                entity.getNotes());
    }

    private static AlertDeliveryPersistenceEntity toPersistenceFromDomain(
            AlertDelivery delivery, AlertPersistenceEntity alert) {
        var entity = new AlertDeliveryPersistenceEntity();
        entity.setId(delivery.getId().value());
        entity.setAlert(alert);
        entity.setRecipientUserId(delivery.getRecipientUserId());
        entity.setRecipientLevel(delivery.getRecipientLevel());
        entity.setChannel(delivery.getChannel());
        entity.setDeliveryStatus(delivery.getDeliveryStatus());
        entity.setSentAt(delivery.getSentAt());
        entity.setDeliveredAt(delivery.getDeliveredAt());
        return entity;
    }

    private static AlertResponsePersistenceEntity toPersistenceFromDomain(
            AlertResponse response, AlertPersistenceEntity alert) {
        var entity = new AlertResponsePersistenceEntity();
        entity.setId(response.getId().value());
        entity.setAlert(alert);
        entity.setResponderUserId(response.getResponderUserId());
        entity.setResponseStatus(response.getResponseStatus());
        entity.setClaimedAt(response.getClaimedAt());
        entity.setCompletedAt(response.getCompletedAt());
        entity.setNotes(response.getNotes());
        return entity;
    }
}
