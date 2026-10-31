package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanPersistenceRepository
        extends JpaRepository<PlanPersistenceEntity, UUID> {

    List<PlanPersistenceEntity> findAllByActiveTrue();
}