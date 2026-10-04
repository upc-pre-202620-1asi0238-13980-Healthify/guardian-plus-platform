package com.healthify.guardian.platform.profile.application.commandservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.commands.CreateUserProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateContactInformationCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateUserProfileCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code UserProfile} aggregate.
 */
public interface UserProfileCommandService {

    Result<UserProfile, ApplicationError> handle(CreateUserProfileCommand command);

    Result<UserProfile, ApplicationError> handle(UpdateUserProfileCommand command);

    Result<UserProfile, ApplicationError> handle(UpdateContactInformationCommand command);
}