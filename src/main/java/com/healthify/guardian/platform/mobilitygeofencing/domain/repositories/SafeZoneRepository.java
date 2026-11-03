package com.healthify.guardian.platform.mobilitygeofencing.domain.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;

import java.util.List;
import java.util.Optional;

public interface SafeZoneRepository {
    SafeZone save(SafeZone safeZone);
    Optional<SafeZone> findById(SafeZoneId id);
    Optional<SafeZone> findActiveByFragileCitizenId(FragileCitizenId fragileCitizenId);
    List<SafeZone> findAllByFragileCitizenId(FragileCitizenId fragileCitizenId);
    void delete(SafeZoneId id);
}