package com.healthify.guardian.platform.profile.domain.model.events;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Raised when a care recipient profile is created.
 */
public record CareRecipientProfileCreatedEvent(
        CareRecipientProfileId careRecipientProfileId,
        UserId createdByUserId,
        String firstName,
        String lastName,
        LocalDate birthDate,
        Instant createdAt) {

    public static CareRecipientProfileCreatedEvent from(CareRecipientProfile profile) {
        return new CareRecipientProfileCreatedEvent(
                profile.getId(),
                profile.getCreatedByUserId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getBirthDate(),
                profile.getCreatedAt());
    }
}