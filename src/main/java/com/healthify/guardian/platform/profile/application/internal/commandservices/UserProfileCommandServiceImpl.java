package com.healthify.guardian.platform.profile.application.internal.commandservices;

import com.healthify.guardian.platform.profile.application.commandservices.UserProfileCommandService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.commands.CreateUserProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateContactInformationCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateUserProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserProfileId;
import com.healthify.guardian.platform.profile.domain.repositories.UserProfileRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateUserProfileImageCommand;

import java.time.Clock;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Application service that executes user profile commands.
 */
@Service
public class UserProfileCommandServiceImpl implements UserProfileCommandService {

    private final UserProfileRepository userProfileRepository;
    private final Clock clock;

    public UserProfileCommandServiceImpl(
            UserProfileRepository userProfileRepository,
            Clock profileClock) {
        this.userProfileRepository = userProfileRepository;
        this.clock = profileClock;
    }

    @Override
    public Result<UserProfile, ApplicationError> handle(CreateUserProfileCommand command) {
        try {
            var profile = new UserProfile(command, clock.instant());

            var existing = userProfileRepository.findByUserId(profile.getUserId());
            if (existing.isPresent()) {
                return Result.failure(ApplicationError.conflict(
                        "UserProfile",
                        resolve("user-profile.already-exists")));
            }

            return Result.success(userProfileRepository.save(profile));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "create-user-profile",
                    resolve(e)));
        }
    }

    @Override
    public Result<UserProfile, ApplicationError> handle(UpdateUserProfileCommand command) {
        return applyToExistingProfile(
                command.userProfileId(),
                "update-user-profile",
                (profile, now) -> profile.updatePersonalInformation(
                        command.firstName(),
                        command.lastName(),
                        now));
    }

    @Override
    public Result<UserProfile, ApplicationError> handle(UpdateContactInformationCommand command) {
        return applyToExistingProfile(
                command.userProfileId(),
                "update-contact-information",
                (profile, now) -> profile.updateContactInformation(
                        command.phoneNumber(),
                        now));
    }
    @Override
    public Result<UserProfile, ApplicationError> handle(
            UpdateUserProfileImageCommand command) {

        return applyToExistingProfile(
                command.userProfileId(),
                "update-user-profile-image",
                (profile, now) -> profile.updateProfileImage(
                        command.profileImageUrl(),
                        now));
    }

    private Result<UserProfile, ApplicationError> applyToExistingProfile(
            UUID userProfileId,
            String operation,
            BiConsumer<UserProfile, java.time.Instant> update) {

        try {
            var id = new UserProfileId(userProfileId);

            var profile = userProfileRepository.findById(id);

            if (profile.isEmpty()) {
                return Result.failure(ApplicationError.notFound(
                        "UserProfile",
                        id.value().toString()));
            }

            update.accept(profile.get(), clock.instant());

            return Result.success(userProfileRepository.save(profile.get()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    operation,
                    resolve(e)));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    operation,
                    resolve(e)));
        }
    }

    private static String resolve(RuntimeException exception) {
        return resolve(exception.getMessage());
    }

    private static String resolve(String messageKey) {
        return MessageResolver.resolveOrDefault(messageKey, messageKey);
    }
}