package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.LocationTracking;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.LocationTrackingRepository;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.LocationRecordJpaEntity;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.LocationTrackingJpaEntity;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories.LocationRecordJpaRepository;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories.LocationTrackingJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LocationTrackingRepositoryImpl implements LocationTrackingRepository {

    private final LocationTrackingJpaRepository trackingJpaRepository;
    private final LocationRecordJpaRepository recordJpaRepository;

    public LocationTrackingRepositoryImpl(LocationTrackingJpaRepository trackingJpaRepository, LocationRecordJpaRepository recordJpaRepository) {
        this.trackingJpaRepository = trackingJpaRepository;
        this.recordJpaRepository = recordJpaRepository;
    }

    @Override
    public LocationTracking save(LocationTracking tracking) {
        // 1. Guardar o actualizar el estado actual del seguimiento
        LocationTrackingJpaEntity entity = new LocationTrackingJpaEntity(
                tracking.getId().value(),
                tracking.getFragileCitizenId().value(),
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().coordinates().latitude() : null,
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().coordinates().longitude() : null,
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().accuracyInMeters() : null,
                tracking.getCurrentStatus() != null ? tracking.getCurrentStatus().name() : null,
                tracking.getLastUpdatedAt()
        );
        trackingJpaRepository.save(entity);

        // 2. Insertar el registro inmutable en la tabla de historial
        if (tracking.getCurrentLocation() != null) {
            LocationRecordJpaEntity recordEntity = new LocationRecordJpaEntity(
                    UUID.randomUUID(),
                    tracking.getId().value(),
                    tracking.getFragileCitizenId().value(),
                    tracking.getCurrentLocation().coordinates().latitude(),
                    tracking.getCurrentLocation().coordinates().longitude(),
                    tracking.getCurrentLocation().accuracyInMeters(),
                    tracking.getCurrentStatus() != null ? tracking.getCurrentStatus().name() : null,
                    tracking.getCurrentLocation().recordedAt()
            );
            recordJpaRepository.save(recordEntity);
        }

        return tracking;
    }

    @Override
    public Optional<LocationTracking> findByFragileCitizenId(FragileCitizenId citizenId) {
        return trackingJpaRepository.findByFragileCitizenId(citizenId.value())
                .map(entity -> {
                    Location currentLocation = null;
                    if (entity.getCurrentLatitude() != null && entity.getCurrentLongitude() != null) {
                        currentLocation = new Location(
                                new Coordinates(entity.getCurrentLatitude(), entity.getCurrentLongitude()),
                                entity.getLastUpdatedAt(),
                                entity.getAccuracyInMeters()
                        );
                    }
                    LocationStatus status = entity.getCurrentStatus() != null ? LocationStatus.valueOf(entity.getCurrentStatus()) : null;
                    return new LocationTracking(
                            new LocationTrackingId(entity.getId()),
                            new FragileCitizenId(entity.getFragileCitizenId()),
                            currentLocation,
                            status,
                            entity.getLastUpdatedAt()
                    );
                });
    }

    @Override
    public List<Location> findHistoryByFragileCitizenId(FragileCitizenId citizenId, Instant periodStart, Instant periodEnd) {
        return recordJpaRepository.findByFragileCitizenIdAndRecordedAtBetween(citizenId.value(), periodStart, periodEnd)
                .stream()
                .map(record -> new Location(
                        new Coordinates(record.getLatitude(), record.getLongitude()),
                        record.getRecordedAt(),
                        record.getAccuracyInMeters()
                ))
                .toList();
    }
}
