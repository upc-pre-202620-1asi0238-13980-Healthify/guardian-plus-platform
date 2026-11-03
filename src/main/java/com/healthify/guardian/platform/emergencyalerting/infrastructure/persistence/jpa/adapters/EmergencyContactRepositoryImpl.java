package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.EmergencyContactId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.EmergencyContactRepository;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers.EmergencyContactPersistenceAssembler;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories.EmergencyContactPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link EmergencyContactRepository} domain port.
 */
@Repository
public class EmergencyContactRepositoryImpl implements EmergencyContactRepository {

    private final EmergencyContactPersistenceRepository emergencyContactPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EmergencyContactRepositoryImpl(
            EmergencyContactPersistenceRepository emergencyContactPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.emergencyContactPersistenceRepository = emergencyContactPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EmergencyContact save(EmergencyContact contact) {
        return saveAll(List.of(contact)).getFirst();
    }

    /** Saves every contact in a single transaction, then publishes their events. */
    @Override
    public List<EmergencyContact> saveAll(List<EmergencyContact> contacts) {
        var savedEntities = emergencyContactPersistenceRepository.saveAll(contacts.stream()
                .map(EmergencyContactPersistenceAssembler::toPersistenceFromDomain)
                .toList());
        var saved = savedEntities.stream().map(EmergencyContactPersistenceAssembler::toDomainFromPersistence).toList();
        var events = contacts.stream().flatMap(contact -> contact.domainEvents().stream()).toList();
        contacts.forEach(EmergencyContact::clearDomainEvents);
        events.forEach(eventPublisher::publishEvent);
        return saved;
    }

    @Override
    public Optional<EmergencyContact> findById(EmergencyContactId id) {
        return emergencyContactPersistenceRepository.findById(id.value())
                .map(EmergencyContactPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<EmergencyContact> findActiveByCareRecipientProfileIdOrderByPriority(
            CareRecipientProfileId careRecipientProfileId) {
        return emergencyContactPersistenceRepository
                .findByCareRecipientProfileIdAndActiveTrueOrderByPriorityOrderAsc(careRecipientProfileId)
                .stream()
                .map(EmergencyContactPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Optional<EmergencyContact> findByCareRecipientProfileIdAndUserId(
            CareRecipientProfileId careRecipientProfileId, UserId userId) {
        return emergencyContactPersistenceRepository.findByCareRecipientProfileIdAndUserId(careRecipientProfileId, userId)
                .map(EmergencyContactPersistenceAssembler::toDomainFromPersistence);
    }
}
