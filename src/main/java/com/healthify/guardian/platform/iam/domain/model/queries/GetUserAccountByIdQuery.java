package com.healthify.guardian.platform.iam.domain.model.queries;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

/**
 * Query for a user account by its identifier.
 */
public record GetUserAccountByIdQuery(UserAccountId userAccountId) {
}
