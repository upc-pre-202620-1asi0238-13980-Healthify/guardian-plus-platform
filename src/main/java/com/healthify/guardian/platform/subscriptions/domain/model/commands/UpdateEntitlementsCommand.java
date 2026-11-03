package com.healthify.guardian.platform.subscriptions.domain.model.commands;

import java.util.Set;
import java.util.UUID;

public record UpdateEntitlementsCommand(
        UUID subscriptionId,
        Set<EntitlementDefinition> entitlements
) {
}