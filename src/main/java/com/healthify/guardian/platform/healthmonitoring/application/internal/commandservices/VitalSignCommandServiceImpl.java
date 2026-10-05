package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EmitVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EvaluateVitalSignsThresholdsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignValue;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignThresholdRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.WearableDeviceRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application service that executes vital sign commands.
 */
@Service
public class VitalSignCommandServiceImpl implements VitalSignCommandService {

    private static final String DEVICE_NOT_ASSIGNED_KEY = "vital-sign.wearable-device.not-assigned";
    private static final String DUPLICATE_KEY = "vital-sign.duplicate";
    private static final String BATCH_EMPTY_KEY = "vital-sign.batch.empty";
    private static final String BATCH_ITEM_INVALID_KEY = "vital-sign.batch.item.invalid";
    private static final String OUTSIDE_PHYSICAL_LIMITS_KEY = "vital-sign.value.outside-physical-limits";
    /** Tolerance applied to the default threshold derived from the catalog normal range. */
    static final int DEFAULT_REQUIRED_CONSECUTIVE_HITS = 3;

    private final VitalSignRepository vitalSignRepository;
    private final VitalSignThresholdRepository vitalSignThresholdRepository;
    private final WearableDeviceRepository wearableDeviceRepository;
    private final VitalSignTypeRepository vitalSignTypeRepository;
    private final Clock clock;

    @Autowired
    public VitalSignCommandServiceImpl(VitalSignRepository vitalSignRepository,
                                       VitalSignThresholdRepository vitalSignThresholdRepository,
                                       WearableDeviceRepository wearableDeviceRepository,
                                       VitalSignTypeRepository vitalSignTypeRepository) {
        this(vitalSignRepository, vitalSignThresholdRepository, wearableDeviceRepository, vitalSignTypeRepository,
                Clock.systemUTC());
    }

    public VitalSignCommandServiceImpl(VitalSignRepository vitalSignRepository,
                                       VitalSignThresholdRepository vitalSignThresholdRepository,
                                       WearableDeviceRepository wearableDeviceRepository,
                                       VitalSignTypeRepository vitalSignTypeRepository,
                                       Clock clock) {
        this.vitalSignRepository = vitalSignRepository;
        this.vitalSignThresholdRepository = vitalSignThresholdRepository;
        this.wearableDeviceRepository = wearableDeviceRepository;
        this.vitalSignTypeRepository = vitalSignTypeRepository;
        this.clock = clock;
    }

    @Override
    public Result<VitalSign, ApplicationError> handle(DetectVitalSignsCommand command) {
        var validation = validate(command);
        if (validation != null) {
            return Result.failure(validation);
        }
        if (isDuplicate(command)) {
            return Result.failure(ApplicationError.conflict("VitalSign", resolve(DUPLICATE_KEY)));
        }
        try {
            var vitalSign = vitalSignRepository.save(new VitalSign(command));
            // Detect -> Emit -> Evaluate run synchronously through domain events, so reload the latest state
            return Result.success(vitalSignRepository.findById(vitalSign.getId()).orElse(vitalSign));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("detect-vital-signs", resolve(e.getMessage())));
        }
    }

    @Override
    public Result<List<VitalSign>, ApplicationError> handleBatch(List<DetectVitalSignsCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return Result.failure(ApplicationError.validationError("telemetry-batch", resolve(BATCH_EMPTY_KEY)));
        }
        var toStore = new ArrayList<VitalSign>();
        var seen = new HashSet<String>();
        for (int index = 0; index < commands.size(); index++) {
            var command = commands.get(index);
            var error = validate(command);
            if (error == null) {
                try {
                    var key = "%s|%s|%s".formatted(command.wearableDeviceId(), command.vitalSignTypeId(), command.measuredAt());
                    if (seen.add(key) && !isDuplicate(command)) {
                        toStore.add(new VitalSign(command));
                    }
                    continue;
                } catch (IllegalArgumentException e) {
                    error = ApplicationError.validationError("telemetry-batch", resolve(e.getMessage()));
                }
            }
            return Result.failure(ApplicationError.businessRuleViolation("telemetry-batch",
                    MessageResolver.resolveOrDefault(BATCH_ITEM_INVALID_KEY, "Reading %d is invalid: %s"
                            .formatted(index, detailsOf(error)), index, detailsOf(error))));
        }
        var saved = vitalSignRepository.saveAll(toStore);
        return Result.success(saved.stream()
                .map(vitalSign -> vitalSignRepository.findById(vitalSign.getId()).orElse(vitalSign))
                .toList());
    }

    @Override
    public Result<VitalSign, ApplicationError> handle(EmitVitalSignsCommand command) {
        var vitalSign = vitalSignRepository.findById(new VitalSignId(command.vitalSignId()));
        if (vitalSign.isEmpty()) {
            return Result.failure(ApplicationError.notFound("VitalSign", command.vitalSignId().toString()));
        }
        try {
            vitalSign.get().emit(Instant.now(clock));
            return Result.success(vitalSignRepository.save(vitalSign.get()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation("emit-vital-signs", resolve(e.getMessage())));
        }
    }

    @Override
    public Result<VitalSign, ApplicationError> handle(EvaluateVitalSignsThresholdsCommand command) {
        var vitalSign = vitalSignRepository.findById(new VitalSignId(command.vitalSignId()));
        if (vitalSign.isEmpty()) {
            return Result.failure(ApplicationError.notFound("VitalSign", command.vitalSignId().toString()));
        }
        var threshold = vitalSignThresholdRepository.findByCareRecipientProfileIdAndVitalSignTypeId(
                vitalSign.get().getCareRecipientProfileId(), vitalSign.get().getVitalSignTypeId());
        if (threshold.isEmpty()) {
            threshold = defineDefaultThreshold(vitalSign.get());
        }
        if (threshold.isEmpty() || !threshold.get().isActive()) {
            return Result.success(vitalSign.get());
        }
        try {
            vitalSign.get().evaluateThresholds(threshold.get(), Instant.now(clock));
            return Result.success(vitalSignRepository.save(vitalSign.get()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "evaluate-vital-signs-thresholds", resolve(e.getMessage())));
        }
    }

    private ApplicationError validate(DetectVitalSignsCommand command) {
        if (command.wearableDeviceId() == null || command.careRecipientProfileId() == null
                || command.vitalSignTypeId() == null) {
            return ApplicationError.validationError("detect-vital-signs",
                    resolve(Objects.isNull(command.wearableDeviceId()) ? "wearable-device.id.invalid"
                            : Objects.isNull(command.careRecipientProfileId()) ? "care-recipient-profile.id.invalid"
                            : "vital-sign-type.id.invalid"));
        }
        var device = wearableDeviceRepository.findById(new WearableDeviceId(command.wearableDeviceId()));
        if (device.isEmpty()) {
            return ApplicationError.notFound("WearableDevice", command.wearableDeviceId().toString());
        }
        if (!device.get().canReportFor(new CareRecipientProfileId(command.careRecipientProfileId()))) {
            return ApplicationError.businessRuleViolation("detect-vital-signs", resolve(DEVICE_NOT_ASSIGNED_KEY));
        }
        var type = vitalSignTypeRepository.findById(new VitalSignTypeId(command.vitalSignTypeId()));
        if (type.isEmpty()) {
            return ApplicationError.notFound("VitalSignType", command.vitalSignTypeId().toString());
        }
        if (command.value() != null && !type.get().isPhysicallyPossible(new VitalSignValue(command.value()))) {
            return ApplicationError.validationError("detect-vital-signs", resolve(OUTSIDE_PHYSICAL_LIMITS_KEY));
        }
        return null;
    }

    /**
     * A care recipient without a personalized threshold is evaluated against the normal range of the
     * vital sign type, materialized as their threshold so the tolerance rule, the live view and the
     * health reports all work from the first reading. A deactivated threshold is never replaced.
     */
    private Optional<VitalSignThreshold> defineDefaultThreshold(VitalSign vitalSign) {
        return vitalSignTypeRepository.findById(vitalSign.getVitalSignTypeId())
                .filter(VitalSignType::hasReferenceRanges)
                .map(type -> vitalSignThresholdRepository.save(new VitalSignThreshold(new DefineVitalSignThresholdCommand(
                        vitalSign.getCareRecipientProfileId().value(),
                        type.getId().value(),
                        type.getNormalRange().minimum(),
                        type.getNormalRange().maximum(),
                        DEFAULT_REQUIRED_CONSECUTIVE_HITS))));
    }

    private boolean isDuplicate(DetectVitalSignsCommand command) {
        return command.measuredAt() != null && vitalSignRepository.existsByWearableDeviceIdAndVitalSignTypeIdAndMeasuredAt(
                new WearableDeviceId(command.wearableDeviceId()),
                new VitalSignTypeId(command.vitalSignTypeId()),
                command.measuredAt());
    }

    private static String detailsOf(ApplicationError error) {
        return error.details() != null ? error.details() : error.message();
    }

    private static String resolve(String key) {
        return MessageResolver.resolveOrDefault(key, key);
    }
}
