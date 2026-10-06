package com.healthify.guardian.platform.profile.application.commandservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateApplicationPreferencesCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateLanguageAndAccessibilityPreferencesCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over user preferences.
 */
public interface UserPreferencesCommandService {

    Result<UserPreferences, ApplicationError> handle(
            UpdateApplicationPreferencesCommand command);

    Result<UserPreferences, ApplicationError> handle(
            UpdateLanguageAndAccessibilityPreferencesCommand command);
}