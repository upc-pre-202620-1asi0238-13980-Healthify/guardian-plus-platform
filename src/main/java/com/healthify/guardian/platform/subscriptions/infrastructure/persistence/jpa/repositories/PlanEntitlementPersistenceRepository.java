package com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanEntitlementId;
import com.healthify.guardian.platform.subscriptions.infrastructure.persistence.jpa.entities.PlanEntitlementPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanEntitlementPersistenceRepository
        extends JpaRepository<PlanEntitlementPersistenceEntity, PlanEntitlementId> {

    List<PlanEntitlementPersistenceEntity> findAllByPlan_Id(UUID planId);

    void deleteAllByPlan_Id(UUID planId);
}