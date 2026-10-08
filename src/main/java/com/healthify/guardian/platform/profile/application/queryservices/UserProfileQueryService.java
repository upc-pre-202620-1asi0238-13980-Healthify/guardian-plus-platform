package com.healthify.guardian.platform.profile.application.queryservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.queries.GetUserProfileByUserIdQuery;

import java.util.Optional;

/**
 * Application service contract for queries over the {@code UserProfile} aggregate.
 */
public interface UserProfileQueryService {

    Optional<UserProfile> handle(GetUserProfileByUserIdQuery query);
}