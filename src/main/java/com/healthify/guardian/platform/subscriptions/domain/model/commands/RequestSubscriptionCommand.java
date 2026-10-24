package com.healthify.guardian.platform.subscriptions.domain.model.commands;

import java.util.UUID;

public record RequestSubscriptionCommand(
        UUID subscriberId,
        UUID planId
) {
}