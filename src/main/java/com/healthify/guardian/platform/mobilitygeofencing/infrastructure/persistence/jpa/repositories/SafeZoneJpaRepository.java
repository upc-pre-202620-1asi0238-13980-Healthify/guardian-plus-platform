package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.SafeZoneJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SafeZoneJpaRepository extends JpaRepository<SafeZoneJpaEntity, UUID> {
    // The most recently changed one, in case zones saved before the single active zone rule left two active
    Optional<SafeZoneJpaEntity> findFirstByFragileCitizenIdAndStatusOrderByUpdatedAtDesc(UUID fragileCitizenId, String status);
    List<SafeZoneJpaEntity> findAllByFragileCitizenIdOrderByCreatedAtAsc(UUID fragileCitizenId);
}
