package com.healthify.guardian.platform.profile.application.internal.commandservices;

import com.healthify.guardian.platform.profile.application.commandservices.UserPreferencesCommandService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.UserPreferences;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateApplicationPreferencesCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateLanguageAndAccessibilityPreferencesCommand;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.repositories.UserPreferencesRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.time.Clock;

/**
 * Application service that executes commands over user preferences.
 */
@Service
public class UserPreferencesCommandServiceImpl
        implements UserPreferencesCommandService {

    private final UserPreferencesRepository userPreferencesRepository;
    private final Clock clock;

    public UserPreferencesCommandServiceImpl(
            UserPreferencesRepository userPreferencesRepository,
            Clock profileClock) {

        this.userPreferencesRepository = userPreferencesRepository;
        this.clock = profileClock;
    }

    @Override
    public Result<UserPreferences, ApplicationError> handle(
            UpdateApplicationPreferencesCommand command) {

        try {
            var userId = new UserId(command.userId());
            var existingPreferences =
                    userPreferencesRepository.findByUserId(userId);

            UserPreferences preferences;

            if (existingPreferences.isPresent()) {
                preferences = existingPreferences.get();
                preferences.updateApplicationPreferences(
                        command.notificationsEnabled(),
                        clock.instant());
            } else {
                preferences = new UserPreferences(
                        userId,
                        command.notificationsEnabled(),
                        clock.instant());
            }

            return Result.success(
                    userPreferencesRepository.save(preferences));

        } catch (IllegalArgumentException e) {
            return Result.failure(
                    ApplicationError.validationError(
                            "update-application-preferences",
                            resolve(e)));
        } catch (IllegalStateException e) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "update-application-preferences",
                            resolve(e)));
        }
    }

    @Override
    public Result<UserPreferences, ApplicationError> handle(
            UpdateLanguageAndAccessibilityPreferencesCommand command) {

        try {
            var userId = new UserId(command.userId());

            var preferences =
                    userPreferencesRepository.findByUserId(userId);

            if (preferences.isEmpty()) {
                return Result.failure(
                        ApplicationError.notFound(
                                "UserPreferences",
                                userId.value().toString()));
            }

            preferences.get()
                    .updateLanguageAndAccessibilityPreferences(
                            command.language(),
                            command.highContrastEnabled(),
                            command.reduceMotionEnabled(),
                            command.fontScale(),
                            clock.instant());

            return Result.success(
                    userPreferencesRepository.save(
                            preferences.get()));

        } catch (IllegalArgumentException e) {
            return Result.failure(
                    ApplicationError.validationError(
                            "update-language-accessibility-preferences",
                            resolve(e)));
        } catch (IllegalStateException e) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "update-language-accessibility-preferences",
                            resolve(e)));
        }
    }

    private static String resolve(RuntimeException exception) {
        return resolve(exception.getMessage());
    }

    private static String resolve(String messageKey) {
        return MessageResolver.resolveOrDefault(
                messageKey,
                messageKey);
    }
}