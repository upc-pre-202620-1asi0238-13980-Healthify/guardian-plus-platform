package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.ProcessedPaymentWebhookEventPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPaymentWebhookEventPersistenceRepository
        extends JpaRepository<
                ProcessedPaymentWebhookEventPersistenceEntity,
                String
        > {
}