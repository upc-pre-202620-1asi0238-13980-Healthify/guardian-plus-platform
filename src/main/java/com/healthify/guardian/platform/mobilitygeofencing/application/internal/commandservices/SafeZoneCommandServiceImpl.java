package com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.CreateSafeZoneCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.ProcessTelemetryCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneViolationEvent;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.LocationPoint;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.SafeZoneRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.services.GeofencingEvaluationService;
import com.healthify.guardian.platform.mobilitygeofencing.interfaces.events.SafeZoneViolationIntegrationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SafeZoneCommandServiceImpl {

    private final SafeZoneRepository safeZoneRepository;
    private final GeofencingEvaluationService geofencingEvaluationService;
    private final ApplicationEventPublisher eventPublisher;

    public SafeZoneCommandServiceImpl(SafeZoneRepository safeZoneRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.safeZoneRepository = safeZoneRepository;
        this.geofencingEvaluationService = new GeofencingEvaluationService();
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SafeZoneId handle(CreateSafeZoneCommand command) {
        CareRecipientProfileId profileId = new CareRecipientProfileId(command.careRecipientProfileId());
        LocationPoint center = new LocationPoint(command.latitude(), command.longitude());

        SafeZone safeZone = new SafeZone(
                SafeZoneId.generate(),
                profileId,
                command.name(),
                center,
                command.radiusInMeters()
        );

        SafeZone saved = safeZoneRepository.save(safeZone);
        return saved.getId();
    }

    @Transactional
    public void handle(ProcessTelemetryCommand command) {
        CareRecipientProfileId profileId = new CareRecipientProfileId(command.careRecipientProfileId());
        LocationPoint currentLocation = new LocationPoint(command.latitude(), command.longitude(), command.accuracy());

        List<SafeZone> activeZones = safeZoneRepository.findAllByCareRecipientProfileIdAndActiveTrue(profileId);

        Optional<SafeZoneViolationEvent> violationOpt = geofencingEvaluationService.evaluateLocation(
                profileId,
                currentLocation,
                activeZones
        );

        if (violationOpt.isPresent()) {
            SafeZoneViolationEvent domainEvent = violationOpt.get();
            // Se publica el evento de integración para desacoplar contextos (p. ej. Emergency & Alerting)
            SafeZoneViolationIntegrationEvent integrationEvent = new SafeZoneViolationIntegrationEvent(
                    domainEvent.careRecipientProfileId().value(),
                    domainEvent.lastLocationPoint().latitude(),
                    domainEvent.lastLocationPoint().longitude(),
                    domainEvent.occurredAt()
            );
            eventPublisher.publishEvent(integrationEvent);
        }
    }
}
