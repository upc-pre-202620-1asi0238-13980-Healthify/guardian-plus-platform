package com.healthify.guardian.platform.emergencyalerting.application.internal.commandservices;

import com.healthify.guardian.platform.emergencyalerting.application.acl.ProfileContextAcl;
import com.healthify.guardian.platform.emergencyalerting.application.commandservices.EmergencyContactCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.AddEmergencyContactCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DeactivateEmergencyContactCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ReorderEmergencyContactsCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.EmergencyContactId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.PhoneNumber;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.PriorityOrder;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.EmergencyContactRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Application service that executes emergency contact commands.
 */
@Service
public class EmergencyContactCommandServiceImpl implements EmergencyContactCommandService {

    private static final String NO_CARE_RELATIONSHIP_MESSAGE_KEY = "emergency-contact.no-care-relationship";
    private static final String ALREADY_REGISTERED_MESSAGE_KEY = "emergency-contact.already-registered";
    private static final String LAST_ACTIVE_MESSAGE_KEY = "emergency-contact.last-active";
    private static final String REORDER_MISMATCH_MESSAGE_KEY = "emergency-contact.reorder.mismatch";

    private final EmergencyContactRepository emergencyContactRepository;
    private final ProfileContextAcl profileContextAcl;

    public EmergencyContactCommandServiceImpl(
            EmergencyContactRepository emergencyContactRepository, ProfileContextAcl profileContextAcl) {
        this.emergencyContactRepository = emergencyContactRepository;
        this.profileContextAcl = profileContextAcl;
    }

    @Override
    public Result<EmergencyContact, ApplicationError> handle(AddEmergencyContactCommand command) {
        try {
            var careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
            var userId = new UserId(command.userId());
            if (!profileContextAcl.hasActiveCareRelationship(careRecipientProfileId, userId)) {
                return Result.failure(ApplicationError.businessRuleViolation(
                        "add-emergency-contact", resolve(NO_CARE_RELATIONSHIP_MESSAGE_KEY)));
            }

            var existing = emergencyContactRepository.findByCareRecipientProfileIdAndUserId(careRecipientProfileId, userId);
            if (existing.isPresent() && existing.get().isActive()) {
                return Result.failure(ApplicationError.conflict("EmergencyContact", resolve(ALREADY_REGISTERED_MESSAGE_KEY)));
            }

            var ranked = new ArrayList<>(
                    emergencyContactRepository.findActiveByCareRecipientProfileIdOrderByPriority(careRecipientProfileId));
            var position = command.priorityOrder() == null
                    ? ranked.size()
                    : Math.min(new PriorityOrder(command.priorityOrder()).value(), ranked.size() + 1) - 1;

            EmergencyContact contact;
            if (existing.isPresent()) {
                contact = existing.get();
                contact.updateContactDetails(
                        command.displayName(), command.relationship(), new PhoneNumber(command.phoneNumber()));
                contact.activate();
            } else {
                contact = new EmergencyContact(new AddEmergencyContactCommand(
                        command.careRecipientProfileId(), command.userId(), command.displayName(),
                        command.relationship(), command.phoneNumber(), position + 1));
            }
            ranked.add(position, contact);
            renumber(ranked);

            var saved = emergencyContactRepository.saveAll(ranked);
            return Result.success(saved.get(position));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("add-emergency-contact", resolve(e.getMessage())));
        } catch (DataIntegrityViolationException e) {
            return Result.failure(ApplicationError.conflict("EmergencyContact", resolve(ALREADY_REGISTERED_MESSAGE_KEY)));
        }
    }

    @Override
    public Result<List<EmergencyContact>, ApplicationError> handle(ReorderEmergencyContactsCommand command) {
        try {
            var careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
            var requested = command.orderedEmergencyContactIds();
            var active = emergencyContactRepository.findActiveByCareRecipientProfileIdOrderByPriority(careRecipientProfileId);

            var activeIds = active.stream().map(contact -> contact.getId().value()).toList();
            if (requested == null
                    || requested.size() != activeIds.size()
                    || !new HashSet<>(requested).equals(new HashSet<>(activeIds))) {
                return Result.failure(ApplicationError.validationError(
                        "reorder-emergency-contacts", resolve(REORDER_MISMATCH_MESSAGE_KEY)));
            }

            var reordered = requested.stream()
                    .map(id -> active.stream().filter(contact -> contact.getId().value().equals(id)).findFirst().orElseThrow())
                    .toList();
            renumber(reordered);
            return Result.success(emergencyContactRepository.saveAll(reordered));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("reorder-emergency-contacts", resolve(e.getMessage())));
        }
    }

    @Override
    public Result<EmergencyContact, ApplicationError> handle(DeactivateEmergencyContactCommand command) {
        if (command.emergencyContactId() == null) {
            return Result.failure(ApplicationError.validationError(
                    "deactivate-emergency-contact", resolve("emergency-contact.id.invalid")));
        }
        var contact = emergencyContactRepository.findById(new EmergencyContactId(command.emergencyContactId()));
        if (contact.isEmpty()) {
            return Result.failure(ApplicationError.notFound("EmergencyContact", command.emergencyContactId().toString()));
        }

        try {
            var remaining = new ArrayList<>(emergencyContactRepository
                    .findActiveByCareRecipientProfileIdOrderByPriority(contact.get().getCareRecipientProfileId()));
            remaining.removeIf(candidate -> candidate.getId().equals(contact.get().getId()));
            if (contact.get().isActive() && remaining.isEmpty()) {
                return Result.failure(ApplicationError.businessRuleViolation(
                        "deactivate-emergency-contact", resolve(LAST_ACTIVE_MESSAGE_KEY)));
            }

            contact.get().deactivate();
            renumber(remaining);
            var toSave = new ArrayList<>(remaining);
            toSave.addFirst(contact.get());
            return Result.success(emergencyContactRepository.saveAll(toSave).getFirst());
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation("deactivate-emergency-contact", resolve(e.getMessage())));
        }
    }

    /** Assigns consecutive priorities, starting at 1, following the list order. */
    private static void renumber(List<EmergencyContact> ranked) {
        for (int index = 0; index < ranked.size(); index++) {
            ranked.get(index).changePriority(new PriorityOrder(index + 1));
        }
    }

    private static String resolve(String messageKey) {
        return MessageResolver.resolveOrDefault(messageKey, messageKey);
    }
}
