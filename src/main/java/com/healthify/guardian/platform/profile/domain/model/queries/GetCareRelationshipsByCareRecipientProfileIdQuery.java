package com.healthify.guardian.platform.profile.domain.model.queries;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;

/**
 * Query to retrieve the active care relationships of a person under care.
 *
 * @param careRecipientProfileId the person under care
 */
public record GetCareRelationshipsByCareRecipientProfileIdQuery(
        CareRecipientProfileId careRecipientProfileId) {
}