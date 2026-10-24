package com.healthify.guardian.platform.profile.domain.model.events;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;

import java.time.Instant;

/**
 * Raised when the personal information of a user profile changes.
 */
public record ProfileUpdatedEvent(
        UserProfileId profileId,
        UserId userId,
        String firstName,
        String lastName,
        String profileImageUrl,
        Instant updatedAt) {

    public static ProfileUpdatedEvent from(UserProfile profile) {
        return new ProfileUpdatedEvent(
                profile.getId(),
                profile.getUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getProfileImageUrl(),
                profile.getUpdatedAt());
    }
}