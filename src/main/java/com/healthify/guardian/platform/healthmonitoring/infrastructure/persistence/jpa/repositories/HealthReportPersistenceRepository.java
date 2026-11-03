package com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.infrastructure.persistence.jpa.entities.HealthReportPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for health report persistence entities.
 */
@Repository
public interface HealthReportPersistenceRepository extends JpaRepository<HealthReportPersistenceEntity, UUID> {

    List<HealthReportPersistenceEntity> findByCareRecipientProfileIdOrderByGeneratedAtDesc(CareRecipientProfileId careRecipientProfileId);
}
