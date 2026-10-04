package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.repositories.CareRecipientProfileRepository;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.assemblers.CareRecipientProfilePersistenceAssembler;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories.CareRecipientProfilePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link CareRecipientProfileRepository} domain port.
 */
@Repository
public class CareRecipientProfileRepositoryImpl
        implements CareRecipientProfileRepository {

    private final CareRecipientProfilePersistenceRepository
            careRecipientProfilePersistenceRepository;

    private final ApplicationEventPublisher eventPublisher;

    public CareRecipientProfileRepositoryImpl(
            CareRecipientProfilePersistenceRepository careRecipientProfilePersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.careRecipientProfilePersistenceRepository =
                careRecipientProfilePersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CareRecipientProfile save(CareRecipientProfile profile) {
        var savedEntity = careRecipientProfilePersistenceRepository.save(
                CareRecipientProfilePersistenceAssembler.toPersistenceFromDomain(profile));

        var saved =
                CareRecipientProfilePersistenceAssembler.toDomainFromPersistence(savedEntity);

        var events = List.copyOf(profile.domainEvents());
        profile.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);

        return saved;
    }

    @Override
    public Optional<CareRecipientProfile> findById(CareRecipientProfileId id) {
        return careRecipientProfilePersistenceRepository.findById(id.value())
                .map(CareRecipientProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<CareRecipientProfile> findByCreatedByUserId(UserId userId) {
        return careRecipientProfilePersistenceRepository
                .findByCreatedByUserId(userId)
                .stream()
                .map(CareRecipientProfilePersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}