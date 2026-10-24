package com.healthify.guardian.platform.profile.domain.model.queries;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

/**
 * Query to retrieve the care recipient profiles created by a user.
 */
public record GetCareRecipientProfilesByCreatedByUserIdQuery(
        UserId createdByUserId) {
}