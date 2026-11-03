package com.healthify.guardian.platform.iam.domain.model.queries;

/**
 * Query that tells whether an e-mail address is still free to register. Takes the raw address so an invalid
 * format can be reported by the caller instead of failing the query.
 */
public record GetEmailAvailabilityQuery(String email) {
}
