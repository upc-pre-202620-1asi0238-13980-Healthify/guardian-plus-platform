package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories;


import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.ZoneViolationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ZoneViolationJpaRepository extends JpaRepository<ZoneViolationJpaEntity, UUID> {
    List<ZoneViolationJpaEntity> findByFragileCitizenId(UUID fragileCitizenId);
    List<ZoneViolationJpaEntity> findBySafeZoneId(UUID safeZoneId);
}
