package com.healthify.guardian.platform.profile.application.queryservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.queries.GetUserPreferencesQuery;

import java.util.Optional;

/**
 * Application service contract for user preference queries.
 */
public interface UserPreferencesQueryService {

    Optional<UserPreferences> handle(
            GetUserPreferencesQuery query);
}