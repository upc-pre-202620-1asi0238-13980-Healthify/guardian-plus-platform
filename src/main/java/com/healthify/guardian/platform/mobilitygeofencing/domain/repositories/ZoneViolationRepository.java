package com.healthify.guardian.platform.mobilitygeofencing.domain.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities.ZoneViolation;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;

import java.util.List;

public interface ZoneViolationRepository {
    ZoneViolation save(ZoneViolation violation);
    List<ZoneViolation> findByFragileCitizenId(FragileCitizenId citizenId);
    List<ZoneViolation> findBySafeZoneId(SafeZoneId safeZoneId);
}