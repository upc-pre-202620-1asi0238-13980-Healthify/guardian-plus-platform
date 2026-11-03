package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.EntitlementPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntitlementPersistenceRepository
        extends JpaRepository<EntitlementPersistenceEntity, String> {
}