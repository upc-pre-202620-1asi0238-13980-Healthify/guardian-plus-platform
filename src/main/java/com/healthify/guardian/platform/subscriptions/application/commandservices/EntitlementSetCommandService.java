package com.healthify.guardian.platform.subscriptions.application.commandservices;

import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.subscriptions.domain.model.aggregates.EntitlementSet;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.RevokeAllEntitlementsCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.SyncEntitlementsFromPlanCommand;
import com.healthify.guardian.platform.subscriptions.domain.model.commands.UpdateEntitlementsCommand;

public interface EntitlementSetCommandService {

    Result<EntitlementSet, ApplicationError> handle(
            UpdateEntitlementsCommand command
    );

    Result<EntitlementSet, ApplicationError> handle(
            SyncEntitlementsFromPlanCommand command
    );

    Result<EntitlementSet, ApplicationError> handle(
            RevokeAllEntitlementsCommand command
    );
}