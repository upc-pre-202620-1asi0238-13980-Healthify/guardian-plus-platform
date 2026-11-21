package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "processed_payment_webhook_events",
        indexes = {
                @Index(
                        name = "ix_processed_payment_webhook_events_provider_event_id",
                        columnList = "provider_event_id"
                ),
                @Index(
                        name = "ix_processed_payment_webhook_events_external_reference",
                        columnList = "external_reference"
                )
        }
)
public class ProcessedPaymentWebhookEventPersistenceEntity {

    @Id
    @Column(
            name = "event_key",
            nullable = false,
            updatable = false,
            length = 255
    )
    private String eventKey;

    @Column(
            name = "provider_event_id",
            nullable = false,
            updatable = false,
            length = 255
    )
    private String providerEventId;

    @Column(
            name = "external_reference",
            nullable = false,
            updatable = false,
            length = 255
    )
    private String externalReference;

    @Column(
            name = "event_type",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "processed_at",
            nullable = false,
            updatable = false
    )
    private Instant processedAt;

    protected ProcessedPaymentWebhookEventPersistenceEntity() {
    }

    public ProcessedPaymentWebhookEventPersistenceEntity(
            String eventKey,
            String providerEventId,
            String externalReference,
            String eventType,
            Instant processedAt
    ) {
        this.eventKey = eventKey;
        this.providerEventId = providerEventId;
        this.externalReference = externalReference;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public String getEventType() {
        return eventType;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}