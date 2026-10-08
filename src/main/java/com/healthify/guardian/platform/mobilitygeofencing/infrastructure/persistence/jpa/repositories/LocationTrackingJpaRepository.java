package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.LocationTrackingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LocationTrackingJpaRepository extends JpaRepository<LocationTrackingJpaEntity, UUID> {
    Optional<LocationTrackingJpaEntity> findByFragileCitizenId(UUID fragileCitizenId);
}
