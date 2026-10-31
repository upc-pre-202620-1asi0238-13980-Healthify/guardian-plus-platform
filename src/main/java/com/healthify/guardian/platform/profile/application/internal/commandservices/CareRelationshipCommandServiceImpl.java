package com.healthify.guardian.platform.profile.application.internal.commandservices;

import com.healthify.guardian.platform.profile.application.commandservices.CareRelationshipCommandService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.commands.EndCareRelationshipCommand;
import com.healthify.guardian.platform.profile.domain.model.commands.EstablishCareRelationshipCommand;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.domain.repositories.CareRecipientProfileRepository;
import com.healthify.guardian.platform.profile.domain.repositories.CareRelationshipRepository;
import com.healthify.guardian.platform.profile.domain.services.CareRelationshipPolicy;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.time.Clock;

/**
 * Application service that executes care relationship commands.
 */
@Service
public class CareRelationshipCommandServiceImpl
        implements CareRelationshipCommandService {

    private static final String RELATIONSHIP_ALREADY_EXISTS_MESSAGE_KEY =
            "care-relationship.already-exists";

    private final CareRelationshipRepository careRelationshipRepository;
    private final CareRecipientProfileRepository careRecipientProfileRepository;
    private final CareRelationshipPolicy careRelationshipPolicy;
    private final Clock clock;

    public CareRelationshipCommandServiceImpl(
            CareRelationshipRepository careRelationshipRepository,
            CareRecipientProfileRepository careRecipientProfileRepository,
            CareRelationshipPolicy careRelationshipPolicy,
            Clock profileClock) {
        this.careRelationshipRepository = careRelationshipRepository;
        this.careRecipientProfileRepository = careRecipientProfileRepository;
        this.careRelationshipPolicy = careRelationshipPolicy;
        this.clock = profileClock;
    }

    @Override
    public Result<CareRelationship, ApplicationError> handle(
            EstablishCareRelationshipCommand command) {

        try {
            var userId = new UserId(command.userId());
            var careRecipientProfileId =
                    new CareRecipientProfileId(command.careRecipientProfileId());

            careRelationshipPolicy.validateRelationshipType(
                    command.relationshipType());

            var careRecipient =
                    careRecipientProfileRepository.findById(careRecipientProfileId);

            if (careRecipient.isEmpty()) {
                return Result.failure(ApplicationError.notFound(
                        "CareRecipientProfile",
                        careRecipientProfileId.value().toString()));
            }

            if (!careRelationshipPolicy.canEstablishRelationship(
                    userId,
                    careRecipientProfileId)) {

                return Result.failure(ApplicationError.conflict(
                        "CareRelationship",
                        resolve(RELATIONSHIP_ALREADY_EXISTS_MESSAGE_KEY)));
            }

            var relationship =
                    new CareRelationship(command, clock.instant());

            return Result.success(
                    careRelationshipRepository.save(relationship));

        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "establish-care-relationship",
                    resolve(e)));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "establish-care-relationship",
                    resolve(e)));
        }
    }

    @Override
    public Result<CareRelationship, ApplicationError> handle(
            EndCareRelationshipCommand command) {

        try {
            var id = new CareRelationshipId(
                    command.careRelationshipId());

            var relationship =
                    careRelationshipRepository.findById(id);

            if (relationship.isEmpty()) {
                return Result.failure(ApplicationError.notFound(
                        "CareRelationship",
                        id.value().toString()));
            }

            relationship.get().end(clock.instant());

            return Result.success(
                    careRelationshipRepository.save(
                            relationship.get()));

        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "end-care-relationship",
                    resolve(e)));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "end-care-relationship",
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