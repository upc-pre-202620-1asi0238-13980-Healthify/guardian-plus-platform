package com.healthify.guardian.platform.iam.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event notifying other contexts that an account verified its e-mail and is now active.
 *
 * @param userAccountId the activated account
 * @param email         its e-mail address
 * @param verifiedAt    when the e-mail was verified
 */
public record UserAccountVerifiedIntegrationEvent(UUID userAccountId, String email, Instant verifiedAt) {
}
