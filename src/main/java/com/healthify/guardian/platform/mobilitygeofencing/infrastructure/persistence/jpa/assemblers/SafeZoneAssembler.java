package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.LocationPoint;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.SafeZoneJpaEntity;

public class SafeZoneAssembler {

    public static SafeZoneJpaEntity toEntity(SafeZone domain) {
        return new SafeZoneJpaEntity(
                domain.getId().value(),
                domain.getCareRecipientProfileId().value(),
                domain.getName(),
                domain.getCenterPoint().latitude(),
                domain.getCenterPoint().longitude(),
                domain.getRadiusInMeters(),
                domain.isActive()
        );
    }

    public static SafeZone toDomain(SafeZoneJpaEntity entity) {
        SafeZone zone = new SafeZone(
                new SafeZoneId(entity.getId()),
                new CareRecipientProfileId(entity.getCareRecipientProfileId()),
                entity.getName(),
                new LocationPoint(entity.getLatitude(), entity.getLongitude()),
                entity.getRadiusInMeters()
        );
        if (Boolean.FALSE.equals(entity.getActive())) {
            zone.deactivate();
        }
        return zone;
    }
}
