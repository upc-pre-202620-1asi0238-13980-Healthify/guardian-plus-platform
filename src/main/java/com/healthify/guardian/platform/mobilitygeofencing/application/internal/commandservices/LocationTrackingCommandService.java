package com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices;

import com.healthify.guardian.platform.mobilitygeofencing.application.ports.inbound.WearableLocationInputPort;
import com.healthify.guardian.platform.mobilitygeofencing.application.ports.inbound.MobilityEventOutputPort;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.LocationTracking;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.ReceiveLocationCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities.ZoneViolation;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneViolationDetectedEvent;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.LocationTrackingRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.SafeZoneRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.ZoneViolationRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.services.GeofenceEvaluationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LocationTrackingCommandService implements WearableLocationInputPort {

    private final SafeZoneRepository safeZoneRepository;
    private final LocationTrackingRepository locationTrackingRepository;
    private final ZoneViolationRepository zoneViolationRepository;
    private final GeofenceEvaluationService geofenceEvaluationService;
    private final MobilityEventOutputPort mobilityEventOutputPort;

    public LocationTrackingCommandService(
            SafeZoneRepository safeZoneRepository,
            LocationTrackingRepository locationTrackingRepository,
            ZoneViolationRepository zoneViolationRepository,
            MobilityEventOutputPort mobilityEventOutputPort) {
        this.safeZoneRepository = safeZoneRepository;
        this.locationTrackingRepository = locationTrackingRepository;
        this.zoneViolationRepository = zoneViolationRepository;
        this.mobilityEventOutputPort = mobilityEventOutputPort;
        this.geofenceEvaluationService = new GeofenceEvaluationService();
    }

    @Override
    @Transactional
    public void receiveLocation(ReceiveLocationCommand command) {
        FragileCitizenId citizenId = new FragileCitizenId(command.fragileCitizenId());
        Location newLocation = new Location(command.coordinates(), command.recordedAt(), command.accuracyInMeters());

        Optional<SafeZone> activeSafeZoneOpt = safeZoneRepository.findActiveByFragileCitizenId(citizenId);

        LocationStatus status = LocationStatus.WITHIN_SAFE_ZONE;
        SafeZone activeZone = null;

        if (activeSafeZoneOpt.isPresent()) {
            activeZone = activeSafeZoneOpt.get();
            status = geofenceEvaluationService.evaluate(newLocation, activeZone.getBoundary());
        }

        LocationTracking tracking = locationTrackingRepository.findByFragileCitizenId(citizenId)
                .orElseGet(() -> new LocationTracking(citizenId));

        tracking.recordLocation(newLocation, status);
        locationTrackingRepository.save(tracking);

        if (status == LocationStatus.OUTSIDE_SAFE_ZONE && activeZone != null) {
            ZoneViolation violation = new ZoneViolation(
                    activeZone.getId(),
                    citizenId,
                    newLocation
            );
            zoneViolationRepository.save(violation);

            SafeZoneViolationDetectedEvent violationEvent = new SafeZoneViolationDetectedEvent(
                    activeZone.getId(),
                    citizenId,
                    newLocation,
                    violation.getDetectedAt()
            );

            // Public Emergency & Alerting
            mobilityEventOutputPort.publish(violationEvent);
        }
    }
}
