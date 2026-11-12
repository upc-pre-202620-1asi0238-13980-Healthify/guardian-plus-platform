package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.CallSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataCallSessionRepository extends JpaRepository<CallSessionJpaEntity, UUID> {
    List<CallSessionJpaEntity> findAllByFragileCitizenIdOrderByStartedAtDesc(UUID fragileCitizenId);
}
