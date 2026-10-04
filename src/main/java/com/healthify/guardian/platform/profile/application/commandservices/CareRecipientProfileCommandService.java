package com.healthify.guardian.platform.profile.application.commandservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.commands.CreateCareRecipientProfileCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code CareRecipientProfile} aggregate.
 */
public interface CareRecipientProfileCommandService {

    Result<CareRecipientProfile, ApplicationError> handle(
            CreateCareRecipientProfileCommand command);
}