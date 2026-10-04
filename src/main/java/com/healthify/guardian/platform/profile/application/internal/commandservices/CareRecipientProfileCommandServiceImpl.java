package com.healthify.guardian.platform.profile.application.internal.commandservices;

import com.healthify.guardian.platform.profile.application.commandservices.CareRecipientProfileCommandService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.commands.CreateCareRecipientProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.UpdateCareRecipientProfileCommand;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.repositories.CareRecipientProfileRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.time.Clock;

/**
 * Application service that executes care recipient profile commands.
 */
@Service
public class CareRecipientProfileCommandServiceImpl
        implements CareRecipientProfileCommandService {

    private final CareRecipientProfileRepository careRecipientProfileRepository;
    private final Clock clock;

    public CareRecipientProfileCommandServiceImpl(
            CareRecipientProfileRepository careRecipientProfileRepository,
            Clock profileClock) {

        this.careRecipientProfileRepository = careRecipientProfileRepository;
        this.clock = profileClock;
    }

    @Override
    public Result<CareRecipientProfile, ApplicationError> handle(
            CreateCareRecipientProfileCommand command) {

        try {
            var profile =
                    new CareRecipientProfile(
                            command,
                            clock.instant());

            return Result.success(
                    careRecipientProfileRepository.save(profile));

        } catch (IllegalArgumentException e) {
            return Result.failure(
                    ApplicationError.validationError(
                            "create-care-recipient-profile",
                            resolve(e)));
        }
    }

    @Override
    public Result<CareRecipientProfile, ApplicationError> handle(
            UpdateCareRecipientProfileCommand command) {

        try {
            var id =
                    new CareRecipientProfileId(
                            command.careRecipientProfileId());

            var profile =
                    careRecipientProfileRepository.findById(id);

            if (profile.isEmpty()) {
                return Result.failure(
                        ApplicationError.notFound(
                                "CareRecipientProfile",
                                id.value().toString()));
            }

            profile.get().updatePersonalInformation(
                    command.firstName(),
                    command.lastName(),
                    command.birthDate(),
                    clock.instant());

            return Result.success(
                    careRecipientProfileRepository.save(
                            profile.get()));

        } catch (IllegalArgumentException e) {
            return Result.failure(
                    ApplicationError.validationError(
                            "update-care-recipient-profile",
                            resolve(e)));

        } catch (IllegalStateException e) {
            return Result.failure(
                    ApplicationError.businessRuleViolation(
                            "update-care-recipient-profile",
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