package com.healthify.guardian.platform.profile.domain.model.queries;

import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

/**
 * Query to retrieve the descriptive profile associated with an IAM user.
 *
 * @param userId the referenced IAM user identifier
 */
public record GetUserProfileByUserIdQuery(UserId userId) {
}