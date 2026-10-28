package com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.events.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.*;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.SafeZoneRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Manages the safe zones of a Fragile Citizen. A Fragile Citizen has at most one active safe zone, the one every
 * location is evaluated against: creating or activating a zone deactivates the one that was active.
 *
 * <p>The update, activate, deactivate and delete commands answer {@code false} when the safe zone does not exist.</p>
 */
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
        deactivateOtherActiveZones(safeZone);
        safeZoneRepository.save(safeZone);
        eventPublisher.publishEvent(new SafeZoneCreatedEvent(safeZone.getId(), safeZone.getFragileCitizenId(), Instant.now()));
        return safeZone.getId();
    }

    @Transactional
    public boolean handle(UpdateSafeZoneCommand command) {
        Optional<SafeZone> found = safeZoneRepository.findById(new SafeZoneId(command.safeZoneId()));
        if (found.isEmpty()) return false;
        SafeZone safeZone = found.get();

        SafeZoneBoundary updatedBoundary = new SafeZoneBoundary(command.center(), command.radiusInMeters());
        safeZone.updateBoundary(command.name(), updatedBoundary);
        safeZoneRepository.save(safeZone);

        eventPublisher.publishEvent(new SafeZoneUpdatedEvent(safeZone.getId()));
        return true;
    }

    @Transactional
    public boolean handle(ActivateSafeZoneCommand command) {
        Optional<SafeZone> found = safeZoneRepository.findById(new SafeZoneId(command.safeZoneId()));
        if (found.isEmpty()) return false;
        SafeZone safeZone = found.get();

        deactivateOtherActiveZones(safeZone);
        safeZone.activate();
        safeZoneRepository.save(safeZone);

        eventPublisher.publishEvent(new SafeZoneActivatedEvent(safeZone.getId()));
        return true;
    }

    @Transactional
    public boolean handle(DeactivateSafeZoneCommand command) {
        Optional<SafeZone> found = safeZoneRepository.findById(new SafeZoneId(command.safeZoneId()));
        if (found.isEmpty()) return false;
        SafeZone safeZone = found.get();

        safeZone.deactivate();
        safeZoneRepository.save(safeZone);

        eventPublisher.publishEvent(new SafeZoneDeactivatedEvent(safeZone.getId()));
        return true;
    }

    @Transactional
    public boolean handle(DeleteSafeZoneCommand command) {
        SafeZoneId safeZoneId = new SafeZoneId(command.safeZoneId());
        if (safeZoneRepository.findById(safeZoneId).isEmpty()) return false;

        safeZoneRepository.delete(safeZoneId);

        eventPublisher.publishEvent(new SafeZoneDeletedEvent(safeZoneId));
        return true;
    }

    @Transactional(readOnly = true)
    public Optional<SafeZone> getActiveByFragileCitizenId(FragileCitizenId fragileCitizenId) {
        return safeZoneRepository.findActiveByFragileCitizenId(fragileCitizenId);
    }

    @Transactional(readOnly = true)
    public List<SafeZone> getAllByFragileCitizenId(FragileCitizenId fragileCitizenId) {
        return safeZoneRepository.findAllByFragileCitizenId(fragileCitizenId);
    }

    // Keeps a single active zone per Fragile Citizen, so the location is never evaluated against two of them
    private void deactivateOtherActiveZones(SafeZone safeZone) {
        safeZoneRepository.findAllByFragileCitizenId(safeZone.getFragileCitizenId()).stream()
                .filter(other -> other.isActive() && !other.getId().equals(safeZone.getId()))
                .forEach(other -> {
                    other.deactivate();
                    safeZoneRepository.save(other);
                    eventPublisher.publishEvent(new SafeZoneDeactivatedEvent(other.getId()));
                });
    }
}
