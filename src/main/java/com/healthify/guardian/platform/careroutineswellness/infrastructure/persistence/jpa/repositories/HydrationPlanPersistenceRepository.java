package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.HydrationPlanPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data repository for hydration plan persistence entities.
 */
@Repository
public interface HydrationPlanPersistenceRepository extends JpaRepository<HydrationPlanPersistenceEntity, UUID> {

    Optional<HydrationPlanPersistenceEntity> findByPersonUnderCareId(PersonUnderCareId personUnderCareId);
}
