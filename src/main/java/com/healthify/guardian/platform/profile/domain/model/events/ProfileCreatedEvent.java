package com.healthify.guardian.platform.profile.domain.model.events;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;

import java.time.Instant;

/**
 * Raised when a user profile is created.
 */
public record ProfileCreatedEvent(
        UserProfileId profileId,
        UserId userId,
        String firstName,
        String lastName,
        Instant createdAt) {

    public static ProfileCreatedEvent from(UserProfile profile) {
        return new ProfileCreatedEvent(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getCreatedAt());
    }
}