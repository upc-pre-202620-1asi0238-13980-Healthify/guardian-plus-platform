package com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.SafeZoneCreatedEvent;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.SafeZoneRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class SafeZoneCommandService {

    private final SafeZoneRepository safeZoneRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SafeZoneCommandService(SafeZoneRepository safeZoneRepository, ApplicationEventPublisher eventPublisher) {
        this.safeZoneRepository = safeZoneRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SafeZoneId handle(CreateSafeZoneCommand command) {
        SafeZone safeZone = new SafeZone(command);
        safeZoneRepository.save(safeZone);

        SafeZoneCreatedEvent event = new SafeZoneCreatedEvent(
                safeZone.getId(),
                safeZone.getFragileCitizenId(),
                Instant.now()
        );
        eventPublisher.publishEvent(event);

        return safeZone.getId();
    }
    @Transactional
    public void handle(UpdateSafeZoneCommand command) {
        SafeZone safeZone = safeZoneRepository.findById(new SafeZoneId(command.safeZoneId()))
                .orElseThrow(() -> new IllegalArgumentException("Geocerca no encontrada con el ID especificado."));

        SafeZoneBoundary updatedBoundary = new SafeZoneBoundary(command.center(), command.radiusInMeters());
        safeZone.updateBoundary(updatedBoundary);

        safeZoneRepository.save(safeZone);
    }

    @Transactional
    public void handle(ActivateSafeZoneCommand command) {
        SafeZone safeZone = safeZoneRepository.findById(new SafeZoneId(command.safeZoneId()))
                .orElseThrow(() -> new IllegalArgumentException("Geocerca no encontrada con el ID especificado."));

        safeZone.activate();
        safeZoneRepository.save(safeZone);
    }

    @Transactional
    public void handle(DeactivateSafeZoneCommand command) {
        SafeZone safeZone = safeZoneRepository.findById(new SafeZoneId(command.safeZoneId()))
                .orElseThrow(() -> new IllegalArgumentException("Geocerca no encontrada con el ID especificado."));

        safeZone.deactivate();
        safeZoneRepository.save(safeZone);
    }

    @Transactional(readOnly = true)
    public Optional<SafeZone> getActiveByFragileCitizenId(FragileCitizenId fragileCitizenId) {
        return safeZoneRepository.findActiveByFragileCitizenId(fragileCitizenId);
    }
}
