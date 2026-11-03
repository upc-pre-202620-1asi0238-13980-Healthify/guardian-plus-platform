package com.healthify.guardian.platform.mobilitygeofencing.application.internal.queryservices;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.LocationTracking;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.queries.GetLocationHistoryQuery;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Location;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.LocationTrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class LocationTrackingQueryService {

    private final LocationTrackingRepository locationTrackingRepository;

    public LocationTrackingQueryService(LocationTrackingRepository locationTrackingRepository) {
        this.locationTrackingRepository = locationTrackingRepository;
    }

    public Optional<LocationTracking> getCurrentTracking(FragileCitizenId fragileCitizenId) {
        return locationTrackingRepository.findByFragileCitizenId(fragileCitizenId);
    }

    public List<Location> handle(GetLocationHistoryQuery query) {
        return locationTrackingRepository.findHistoryByFragileCitizenId(
                query.fragileCitizenId(),
                query.start(),
                query.end()
        );
    }
}
