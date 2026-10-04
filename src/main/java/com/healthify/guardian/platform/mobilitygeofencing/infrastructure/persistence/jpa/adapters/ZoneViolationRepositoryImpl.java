package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities.ZoneViolation;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.ZoneViolationRepository;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.ZoneViolationJpaEntity;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories.ZoneViolationJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ZoneViolationRepositoryImpl implements ZoneViolationRepository {

    private final ZoneViolationJpaRepository jpaRepository;

    public ZoneViolationRepositoryImpl(ZoneViolationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ZoneViolation save(ZoneViolation violation) {
        ZoneViolationJpaEntity entity = new ZoneViolationJpaEntity(
                violation.getId().value(),
                violation.getSafeZoneId().value(),
                violation.getFragileCitizenId().value(),
                violation.getLocation().coordinates().latitude(),
                violation.getLocation().coordinates().longitude(),
                violation.getLocation().accuracyInMeters(),
                violation.getDetectedAt()
        );
        jpaRepository.save(entity);
        return violation;
    }

    @Override
    public List<ZoneViolation> findByFragileCitizenId(FragileCitizenId citizenId) {
        return jpaRepository.findByFragileCitizenId(citizenId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<ZoneViolation> findBySafeZoneId(SafeZoneId safeZoneId) {
        return jpaRepository.findBySafeZoneId(safeZoneId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private ZoneViolation toDomain(ZoneViolationJpaEntity entity) {
        Location location = new Location(
                new Coordinates(entity.getLatitude(), entity.getLongitude()),
                entity.getDetectedAt(),
                entity.getAccuracyInMeters()
        );
        return new ZoneViolation(
                new ZoneViolationId(entity.getId()),
                new SafeZoneId(entity.getSafeZoneId()),
                new FragileCitizenId(entity.getFragileCitizenId()),
                location,
                entity.getDetectedAt()
        );
    }
}
