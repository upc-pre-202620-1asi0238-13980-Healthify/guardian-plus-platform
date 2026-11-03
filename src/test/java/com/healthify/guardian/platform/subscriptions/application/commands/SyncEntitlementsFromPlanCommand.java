package com.healthify.guardian.platform.subscriptions.application.commands;

import java.util.Objects;
import java.util.UUID;

public record SyncEntitlementsFromPlanCommand(
        UUID subscriptionId,
        UUID planId
) {

    public SyncEntitlementsFromPlanCommand {
        Objects.requireNonNull(
                subscriptionId,
                "subscriptionId must not be null"
        );

        Objects.requireNonNull(
                planId,
                "planId must not be null"
        );
    }
}