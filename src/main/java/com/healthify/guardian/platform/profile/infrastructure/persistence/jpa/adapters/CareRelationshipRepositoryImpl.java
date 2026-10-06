package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipStatus;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.repositories.CareRelationshipRepository;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.assemblers.CareRelationshipPersistenceAssembler;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories.CareRelationshipPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link CareRelationshipRepository} domain port.
 */
@Repository
public class CareRelationshipRepositoryImpl
        implements CareRelationshipRepository {

    private final CareRelationshipPersistenceRepository
            careRelationshipPersistenceRepository;

    private final ApplicationEventPublisher eventPublisher;

    public CareRelationshipRepositoryImpl(
            CareRelationshipPersistenceRepository careRelationshipPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.careRelationshipPersistenceRepository =
                careRelationshipPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CareRelationship save(CareRelationship relationship) {
        var savedEntity = careRelationshipPersistenceRepository.save(
                CareRelationshipPersistenceAssembler
                        .toPersistenceFromDomain(relationship));

        var saved =
                CareRelationshipPersistenceAssembler
                        .toDomainFromPersistence(savedEntity);

        var events = List.copyOf(relationship.domainEvents());
        relationship.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);

        return saved;
    }

    @Override
    public Optional<CareRelationship> findById(CareRelationshipId id) {
        return careRelationshipPersistenceRepository
                .findById(id.value())
                .map(CareRelationshipPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<CareRelationship> findActiveByUserId(UserId userId) {
        return careRelationshipPersistenceRepository
                .findByUserIdAndStatus(
                        userId,
                        CareRelationshipStatus.ACTIVE)
                .stream()
                .map(CareRelationshipPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<CareRelationship> findActiveByCareRecipientId(
            CareRecipientProfileId careRecipientProfileId) {

        return careRelationshipPersistenceRepository
                .findByCareRecipientProfileIdAndStatus(
                        careRecipientProfileId,
                        CareRelationshipStatus.ACTIVE)
                .stream()
                .map(CareRelationshipPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}