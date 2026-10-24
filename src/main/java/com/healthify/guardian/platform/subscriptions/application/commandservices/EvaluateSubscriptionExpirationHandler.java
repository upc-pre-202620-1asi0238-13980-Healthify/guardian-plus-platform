package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionExpirationCommand;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.ExpirationEvaluation;

public interface EvaluateSubscriptionExpirationHandler {

    ExpirationEvaluation handle(
            EvaluateSubscriptionExpirationCommand command
    );
}