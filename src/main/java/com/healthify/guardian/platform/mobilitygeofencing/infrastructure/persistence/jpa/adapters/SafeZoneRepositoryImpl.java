package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneStatus;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.SafeZoneRepository;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.assemblers.SafeZoneAssembler;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.SafeZoneJpaEntity;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories.SafeZoneJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SafeZoneRepositoryImpl implements SafeZoneRepository {

    private final SafeZoneJpaRepository jpaRepository;

    public SafeZoneRepositoryImpl(SafeZoneJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SafeZone save(SafeZone safeZone) {
        SafeZoneJpaEntity entity = SafeZoneAssembler.toEntity(safeZone);
        SafeZoneJpaEntity savedEntity = jpaRepository.save(entity);
        return SafeZoneAssembler.toDomain(savedEntity);
    }

    @Override
    public Optional<SafeZone> findById(SafeZoneId id) {
        return jpaRepository.findById(id.value())
                .map(SafeZoneAssembler::toDomain);
    }

    @Override
    public Optional<SafeZone> findActiveByFragileCitizenId(FragileCitizenId fragileCitizenId) {
        return jpaRepository.findByFragileCitizenIdAndStatus(
                fragileCitizenId.value(),
                SafeZoneStatus.ACTIVE.name()
        ).map(SafeZoneAssembler::toDomain);
    }

    @Override
    public List<SafeZone> findAllByFragileCitizenId(FragileCitizenId fragileCitizenId) {
        return jpaRepository.findAllByFragileCitizenId(fragileCitizenId.value())
                .stream()
                .map(SafeZoneAssembler::toDomain)
                .toList();
    }

    @Override
    public void delete(SafeZoneId id) {
        jpaRepository.deleteById(id.value());
    }
}
