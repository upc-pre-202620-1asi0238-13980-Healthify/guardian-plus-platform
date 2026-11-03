package com.healthify.guardian.platform.iam.interfaces.rest.resources;

/**
 * Response payload telling whether an e-mail is still free to register.
 */
public record EmailAvailabilityResource(
        String email,
        boolean available
) {
}
