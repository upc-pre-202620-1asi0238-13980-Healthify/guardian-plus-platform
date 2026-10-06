package com.healthify.guardian.platform.profile.application.internal.queryservices;

import com.healthify.guardian.platform.profile.application.queryservices.UserPreferencesQueryService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.queries.GetUserPreferencesQuery;
import com.healthify.guardian.platform.profile.domain.repositories.UserPreferencesRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that executes user preference queries.
 */
@Service
public class UserPreferencesQueryServiceImpl
        implements UserPreferencesQueryService {

    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesQueryServiceImpl(
            UserPreferencesRepository userPreferencesRepository) {

        this.userPreferencesRepository =
                userPreferencesRepository;
    }

    @Override
    public Optional<UserPreferences> handle(
            GetUserPreferencesQuery query) {

        return userPreferencesRepository
                .findByUserId(query.userId());
    }
}