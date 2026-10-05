package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignThresholdCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.ActivateVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DeactivateVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignThresholdId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignThresholdRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Application service that executes vital sign threshold commands.
 */
@Service
public class VitalSignThresholdCommandServiceImpl implements VitalSignThresholdCommandService {

    private static final String OUTSIDE_PHYSICAL_LIMITS_KEY = "vital-sign-threshold.range.outside-physical-limits";

    private final VitalSignThresholdRepository vitalSignThresholdRepository;
    private final VitalSignTypeRepository vitalSignTypeRepository;

    public VitalSignThresholdCommandServiceImpl(VitalSignThresholdRepository vitalSignThresholdRepository,
                                                VitalSignTypeRepository vitalSignTypeRepository) {
        this.vitalSignThresholdRepository = vitalSignThresholdRepository;
        this.vitalSignTypeRepository = vitalSignTypeRepository;
    }

    @Override
    public Result<VitalSignThreshold, ApplicationError> handle(DefineVitalSignThresholdCommand command) {
        try {
            var careRecipientProfileId = new CareRecipientProfileId(command.careRecipientProfileId());
            var vitalSignTypeId = new VitalSignTypeId(command.vitalSignTypeId());
            var type = vitalSignTypeRepository.findById(vitalSignTypeId);
            if (type.isEmpty()) {
                return Result.failure(ApplicationError.notFound("VitalSignType", command.vitalSignTypeId().toString()));
            }
            if (exceedsPhysicalLimits(type.get(), command)) {
                return Result.failure(ApplicationError.validationError("define-vital-sign-threshold",
                        MessageResolver.resolveOrDefault(OUTSIDE_PHYSICAL_LIMITS_KEY, OUTSIDE_PHYSICAL_LIMITS_KEY)));
            }
            var existing = vitalSignThresholdRepository
                    .findByCareRecipientProfileIdAndVitalSignTypeId(careRecipientProfileId, vitalSignTypeId);
            if (existing.isPresent()) {
                existing.get().redefine(command.minimumValue(), command.maximumValue(), command.requiredConsecutiveHits());
                return Result.success(vitalSignThresholdRepository.save(existing.get()));
            }
            return Result.success(vitalSignThresholdRepository.save(new VitalSignThreshold(command)));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "define-vital-sign-threshold", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }

    @Override
    public Result<VitalSignThreshold, ApplicationError> handle(ActivateVitalSignThresholdCommand command) {
        return changeState(command.vitalSignThresholdId(), VitalSignThreshold::activate, "activate-vital-sign-threshold");
    }

    @Override
    public Result<VitalSignThreshold, ApplicationError> handle(DeactivateVitalSignThresholdCommand command) {
        return changeState(command.vitalSignThresholdId(), VitalSignThreshold::deactivate, "deactivate-vital-sign-threshold");
    }

    /**
     * A personalized range may be narrower or wider than the normal one, but never beyond what the
     * vital sign type can physically measure. Malformed ranges are left to the aggregate to reject.
     */
    private static boolean exceedsPhysicalLimits(VitalSignType type, DefineVitalSignThresholdCommand command) {
        if (command.minimumValue() == null || command.maximumValue() == null
                || command.minimumValue().compareTo(command.maximumValue()) > 0) {
            return false;
        }
        return !type.allows(new VitalSignRange(command.minimumValue(), command.maximumValue()));
    }

    private Result<VitalSignThreshold, ApplicationError> changeState(
            UUID thresholdId, Consumer<VitalSignThreshold> transition, String rule) {
        var threshold = vitalSignThresholdRepository.findById(new VitalSignThresholdId(thresholdId));
        if (threshold.isEmpty()) {
            return Result.failure(ApplicationError.notFound("VitalSignThreshold", thresholdId.toString()));
        }
        try {
            transition.accept(threshold.get());
            return Result.success(vitalSignThresholdRepository.save(threshold.get()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    rule, MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }
}
