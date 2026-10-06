package com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.repositories.UserPreferencesRepository;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.assemblers.UserPreferencesPersistenceAssembler;
import com.healthify.guardian.platform.profile.infrastructure.persistence.jpa.repositories.UserPreferencesPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link UserPreferencesRepository} domain port.
 */
@Repository
public class UserPreferencesRepositoryImpl
        implements UserPreferencesRepository {

    private final UserPreferencesPersistenceRepository
            userPreferencesPersistenceRepository;

    private final ApplicationEventPublisher eventPublisher;

    public UserPreferencesRepositoryImpl(
            UserPreferencesPersistenceRepository userPreferencesPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {

        this.userPreferencesPersistenceRepository =
                userPreferencesPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserPreferences save(UserPreferences preferences) {

        var savedEntity =
                userPreferencesPersistenceRepository.save(
                        UserPreferencesPersistenceAssembler
                                .toPersistenceFromDomain(preferences));

        var saved =
                UserPreferencesPersistenceAssembler
                        .toDomainFromPersistence(savedEntity);

        var events = List.copyOf(preferences.domainEvents());
        preferences.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);

        return saved;
    }

    @Override
    public Optional<UserPreferences> findByUserId(UserId userId) {

        return userPreferencesPersistenceRepository
                .findById(userId.value())
                .map(UserPreferencesPersistenceAssembler
                        ::toDomainFromPersistence);
    }
}