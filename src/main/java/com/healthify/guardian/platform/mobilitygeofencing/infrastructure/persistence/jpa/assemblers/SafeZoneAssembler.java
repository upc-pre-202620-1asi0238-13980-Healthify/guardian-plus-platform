package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.SafeZoneJpaEntity;

public class SafeZoneAssembler {

    public static SafeZoneJpaEntity toEntity(SafeZone safeZone) {
        return new SafeZoneJpaEntity(
                safeZone.getId().value(),
                safeZone.getFragileCitizenId().value(),
                safeZone.getName(),
                safeZone.getBoundary().center().latitude(),
                safeZone.getBoundary().center().longitude(),
                safeZone.getBoundary().radiusInMeters(),
                safeZone.getStatus().name(),
                safeZone.getCreatedAt(),
                safeZone.getUpdatedAt()
        );
    }

    public static SafeZone toDomain(SafeZoneJpaEntity entity) {
        Coordinates center = new Coordinates(entity.getCenterLatitude(), entity.getCenterLongitude());
        SafeZoneBoundary boundary = new SafeZoneBoundary(center, entity.getRadiusInMeters());

        return new SafeZone(
                new SafeZoneId(entity.getId()),
                new FragileCitizenId(entity.getFragileCitizenId()),
                entity.getName(),
                boundary,
                SafeZoneStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
