package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.LocationRecordJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LocationRecordJpaRepository extends JpaRepository<LocationRecordJpaEntity, UUID> {
    List<LocationRecordJpaEntity> findByFragileCitizenIdAndRecordedAtBetween(UUID fragileCitizenId, Instant start, Instant end);
}
