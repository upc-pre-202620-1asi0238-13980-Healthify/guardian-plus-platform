package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.SafeZoneJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SafeZoneJpaRepository extends JpaRepository<SafeZoneJpaEntity, UUID> {
    Optional<SafeZoneJpaEntity> findByFragileCitizenIdAndStatus(UUID fragileCitizenId, String status);
    List<SafeZoneJpaEntity> findAllByFragileCitizenId(UUID fragileCitizenId);
}
