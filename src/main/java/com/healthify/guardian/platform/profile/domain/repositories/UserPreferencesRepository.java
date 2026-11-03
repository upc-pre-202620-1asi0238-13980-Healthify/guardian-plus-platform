package com.healthify.guardian.platform.profile.domain.repositories;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

import java.util.Optional;

/**
 * Repository port for the {@link UserPreferences} aggregate.
 */
public interface UserPreferencesRepository {

    UserPreferences save(UserPreferences preferences);

    Optional<UserPreferences> findByUserId(UserId userId);
}