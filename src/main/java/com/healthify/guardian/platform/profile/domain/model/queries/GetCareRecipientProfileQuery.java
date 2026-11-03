package com.healthify.guardian.platform.profile.domain.model.queries;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query to retrieve a care recipient profile by its identifier.
 *
 * @param careRecipientProfileId the profile identifier
 */
public record GetCareRecipientProfileQuery(
        CareRecipientProfileId careRecipientProfileId) {
}