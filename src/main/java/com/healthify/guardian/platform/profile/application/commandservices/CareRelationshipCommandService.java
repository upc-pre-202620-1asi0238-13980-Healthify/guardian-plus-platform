package com.healthify.guardian.platform.profile.application.commandservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.commands.EndCareRelationshipCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.EstablishCareRelationshipCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code CareRelationship} aggregate.
 */
public interface CareRelationshipCommandService {

    Result<CareRelationship, ApplicationError> handle(
            EstablishCareRelationshipCommand command);

    Result<CareRelationship, ApplicationError> handle(
            EndCareRelationshipCommand command);
}