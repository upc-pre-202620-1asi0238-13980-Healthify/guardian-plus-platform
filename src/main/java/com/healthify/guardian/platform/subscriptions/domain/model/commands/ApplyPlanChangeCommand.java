package com.healthify.guardian.platform.subscriptions.domain.model.commands;

import java.util.UUID;

public record ApplyPlanChangeCommand(
        UUID subscriptionId,
        UUID newPlanId
) {
}