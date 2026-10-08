package com.healthify.guardian.platform.profile.domain.repositories;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;

import java.util.Optional;

/**
 * Repository port for the {@link UserProfile} aggregate.
 */
public interface UserProfileRepository {

    /**
     * Persists a user profile.
     *
     * @param profile the profile to save
     * @return the saved profile
     */
    UserProfile save(UserProfile profile);

    /**
     * Retrieves a profile by its identifier.
     *
     * @param id the profile identifier
     * @return the profile, if found
     */
    Optional<UserProfile> findById(UserProfileId id);

    /**
     * Retrieves the descriptive profile associated with an IAM user.
     *
     * @param userId the referenced IAM user identifier
     * @return the profile, if found
     */
    Optional<UserProfile> findByUserId(UserId userId);
}