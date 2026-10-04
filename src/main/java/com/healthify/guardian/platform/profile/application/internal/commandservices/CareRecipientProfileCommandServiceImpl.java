package com.healthify.guardian.platform.profile.application.internal.commandservices;

import com.healthify.guardian.platform.profile.application.commandservices.CareRecipientProfileCommandService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.commands.CreateCareRecipientProfileCommand;
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
            var profile = new CareRecipientProfile(command, clock.instant());

            return Result.success(
                    careRecipientProfileRepository.save(profile));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "create-care-recipient-profile",
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