package com.healthify.guardian.platform.subscriptions.infrastructure.scheduling;

public enum SubscriptionLifecycleExecutionResult {

    NONE,
    RENEWED,
    RENEWAL_FAILED,
    CANCELLED,
    EXPIRED
}