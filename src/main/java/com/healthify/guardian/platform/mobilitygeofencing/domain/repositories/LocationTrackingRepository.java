package com.healthify.guardian.platform.mobilitygeofencing.domain.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.LocationTracking;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Location;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LocationTrackingRepository {
    LocationTracking save(LocationTracking tracking);
    Optional<LocationTracking> findByFragileCitizenId(FragileCitizenId citizenId);
    List<Location> findHistoryByFragileCitizenId(FragileCitizenId citizenId, Instant periodStart, Instant periodEnd);
}
