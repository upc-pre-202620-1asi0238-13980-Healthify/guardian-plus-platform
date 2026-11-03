package com.healthify.guardian.platform.profile.domain.model.events;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;

import java.time.Instant;

/**
 * Raised when the contact information of a user profile changes.
 */
public record ContactInformationUpdatedEvent(
        UserProfileId profileId,
        UserId userId,
        String phoneNumber,
        Instant updatedAt) {

    public static ContactInformationUpdatedEvent from(UserProfile profile) {
        return new ContactInformationUpdatedEvent(
                profile.getId(),
                profile.getUserId(),
                profile.getPhoneNumber(),
                profile.getUpdatedAt());
    }
}