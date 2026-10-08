package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.MedicationStockPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for medication stock persistence entities.
 */
@Repository
public interface MedicationStockPersistenceRepository extends JpaRepository<MedicationStockPersistenceEntity, UUID> {

    List<MedicationStockPersistenceEntity> findByPersonUnderCareIdOrderByMedicationName(PersonUnderCareId personUnderCareId);
}
