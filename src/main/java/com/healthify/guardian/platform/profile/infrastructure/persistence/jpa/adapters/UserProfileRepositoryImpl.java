package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;
import com.healthify.guardian.platform.profile.domain.repositories.UserProfileRepository;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.assemblers.UserProfilePersistenceAssembler;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories.UserProfilePersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link UserProfileRepository} domain port.
 */
@Repository
public class UserProfileRepositoryImpl implements UserProfileRepository {

    private final UserProfilePersistenceRepository userProfilePersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserProfileRepositoryImpl(
            UserProfilePersistenceRepository userProfilePersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.userProfilePersistenceRepository = userProfilePersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserProfile save(UserProfile profile) {
        var savedEntity = userProfilePersistenceRepository.save(
                UserProfilePersistenceAssembler.toPersistenceFromDomain(profile));

        var saved = UserProfilePersistenceAssembler.toDomainFromPersistence(savedEntity);

        var events = List.copyOf(profile.domainEvents());
        profile.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);

        return saved;
    }

    @Override
    public Optional<UserProfile> findById(UserProfileId id) {
        return userProfilePersistenceRepository.findById(id.value())
                .map(UserProfilePersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<UserProfile> findByUserId(UserId userId) {
        return userProfilePersistenceRepository.findByUserId(userId)
                .map(UserProfilePersistenceAssembler::toDomainFromPersistence);
    }
}