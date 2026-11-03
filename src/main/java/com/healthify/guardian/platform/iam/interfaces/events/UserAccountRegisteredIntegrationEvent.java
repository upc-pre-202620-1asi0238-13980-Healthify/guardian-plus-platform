package com.healthify.guardian.platform.iam.interfaces.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Integration event notifying other contexts (e.g. Profile) that a new identity was registered, so they can create
 * what they associate with it.
 *
 * @param userAccountId the new account
 * @param email         its e-mail address
 * @param registeredAt  when it was registered
 */
public record UserAccountRegisteredIntegrationEvent(UUID userAccountId, String email, Instant registeredAt) {
}
