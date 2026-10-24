package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionLifecycleCommand;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.SubscriptionLifecycleAction;

public interface EvaluateSubscriptionLifecycleHandler {

    SubscriptionLifecycleAction handle(
            EvaluateSubscriptionLifecycleCommand command
    );
}