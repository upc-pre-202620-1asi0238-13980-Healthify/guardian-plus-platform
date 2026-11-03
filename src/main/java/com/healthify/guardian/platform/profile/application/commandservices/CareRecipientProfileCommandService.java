package com.healthify.guardian.platform.profile.application.commandservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.commands.CreateCareRecipientProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateCareRecipientProfileCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateCareRecipientProfileImageCommand;

/**
 * Application service contract for commands over the {@code CareRecipientProfile} aggregate.
 */
public interface CareRecipientProfileCommandService {

    Result<CareRecipientProfile, ApplicationError> handle(
            CreateCareRecipientProfileCommand command);

    Result<CareRecipientProfile, ApplicationError> handle(
            UpdateCareRecipientProfileCommand command);
    Result<CareRecipientProfile, ApplicationError> handle(
            UpdateCareRecipientProfileImageCommand command);
}