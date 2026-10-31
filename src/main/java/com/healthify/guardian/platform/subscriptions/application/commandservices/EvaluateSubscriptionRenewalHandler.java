package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.subscriptions.application.commands.EvaluateSubscriptionRenewalCommand;
import com.healthify.guardian.platform.subscriptions.application.lifecycle.RenewalEvaluation;

public interface EvaluateSubscriptionRenewalHandler {

    RenewalEvaluation handle(
            EvaluateSubscriptionRenewalCommand command
    );
}