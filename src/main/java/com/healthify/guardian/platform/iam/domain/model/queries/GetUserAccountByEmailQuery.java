package com.healthify.guardian.platform.iam.domain.model.queries;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;

/**
 * Query for a user account by its e-mail address.
 */
public record GetUserAccountByEmailQuery(Email email) {
}
