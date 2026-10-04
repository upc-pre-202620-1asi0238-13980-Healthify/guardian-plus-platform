package com.healthify.guardian.platform.mobilitygeofencing.domain.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;

import java.util.List;
import java.util.Optional;

public interface SafeZoneRepository {
    SafeZone save(SafeZone safeZone);
    Optional<SafeZone> findById(SafeZoneId id);
    List<SafeZone> findAllByCareRecipientProfileIdAndActiveTrue(CareRecipientProfileId profileId);
    List<SafeZone> findAllByCareRecipientProfileId(CareRecipientProfileId profileId);
}
