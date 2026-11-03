package com.healthify.guardian.platform.subscriptions.interfaces.rest.resources;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record EntitlementSetResource(
        UUID subscriptionId,
        Set<EntitlementResource> entitlements,
        Instant updatedAt
) {
}