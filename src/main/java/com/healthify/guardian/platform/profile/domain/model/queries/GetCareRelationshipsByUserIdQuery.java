package com.healthify.guardian.platform.profile.domain.model.queries;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

/**
 * Query to retrieve the active care relationships of a user.
 *
 * @param userId the referenced IAM user
 */
public record GetCareRelationshipsByUserIdQuery(UserId userId) {
}