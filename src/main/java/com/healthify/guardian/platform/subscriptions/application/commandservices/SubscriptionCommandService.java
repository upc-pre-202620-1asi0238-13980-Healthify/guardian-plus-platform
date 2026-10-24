package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.Subscription;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ActivateSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ApplyPlanChangeCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.CancelSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.ExpireSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RenewSubscriptionCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCancellationCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RequestSubscriptionCommand;

public interface SubscriptionCommandService {

    Result<Subscription, ApplicationError> handle(
            RequestSubscriptionCommand command
    );

    Result<Subscription, ApplicationError> handle(
            ActivateSubscriptionCommand command
    );

    Result<Subscription, ApplicationError> handle(
            ApplyPlanChangeCommand command
    );

    Result<Subscription, ApplicationError> handle(
            RenewSubscriptionCommand command
    );

    Result<Subscription, ApplicationError> handle(
            RequestSubscriptionCancellationCommand command
    );

    Result<Subscription, ApplicationError> handle(
            CancelSubscriptionCommand command
    );

    Result<Subscription, ApplicationError> handle(
            ExpireSubscriptionCommand command
    );
}