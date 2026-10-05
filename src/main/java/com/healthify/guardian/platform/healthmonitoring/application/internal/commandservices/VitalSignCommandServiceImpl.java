package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EmitVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EvaluateVitalSignsThresholdsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.WearableDeviceId;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignRepository;
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

/**
 * Application service that executes vital sign commands.
 */
@Service
public class VitalSignCommandServiceImpl implements VitalSignCommandService {

    private static final String DEVICE_NOT_LINKED_KEY = "vital-sign.wearable-device.not-linked";
    private static final String DUPLICATE_KEY = "vital-sign.duplicate";
    private static final String BATCH_EMPTY_KEY = "vital-sign.batch.empty";
    private static final String BATCH_ITEM_INVALID_KEY = "vital-sign.batch.item.invalid";

    private final VitalSignRepository vitalSignRepository;
    private final WearableDeviceRepository wearableDeviceRepository;
    private final Clock clock;

    @Autowired
    public VitalSignCommandServiceImpl(VitalSignRepository vitalSignRepository,
                                       WearableDeviceRepository wearableDeviceRepository) {
        this(vitalSignRepository, wearableDeviceRepository, Clock.systemUTC());
    }

    public VitalSignCommandServiceImpl(VitalSignRepository vitalSignRepository,
                                       WearableDeviceRepository wearableDeviceRepository,
                                       Clock clock) {
        this.vitalSignRepository = vitalSignRepository;
        this.wearableDeviceRepository = wearableDeviceRepository;
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
                    var key = "%s|%s|%s".formatted(command.wearableDeviceId(),
                            VitalSignType.fromCode(command.vitalSignType()), command.measuredAt());
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
        try {
            vitalSign.get().evaluateThresholds(Instant.now(clock));
            return Result.success(vitalSignRepository.save(vitalSign.get()));
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation(
                    "evaluate-vital-signs-thresholds", resolve(e.getMessage())));
        }
    }

    private ApplicationError validate(DetectVitalSignsCommand command) {
        if (command.wearableDeviceId() == null) {
            return ApplicationError.validationError("detect-vital-signs", resolve("wearable-device.id.invalid"));
        }
        if (command.careRecipientProfileId() == null) {
            return ApplicationError.validationError("detect-vital-signs", resolve("care-recipient-profile.id.invalid"));
        }
        try {
            VitalSignType.fromCode(command.vitalSignType());
        } catch (IllegalArgumentException e) {
            return ApplicationError.validationError("detect-vital-signs", resolve(e.getMessage()));
        }
        var device = wearableDeviceRepository.findById(new WearableDeviceId(command.wearableDeviceId()));
        if (device.isEmpty()) {
            return ApplicationError.notFound("WearableDevice", command.wearableDeviceId().toString());
        }
        if (!device.get().canReportFor(new CareRecipientProfileId(command.careRecipientProfileId()))) {
            return ApplicationError.businessRuleViolation("detect-vital-signs", resolve(DEVICE_NOT_LINKED_KEY));
        }
        return null;
    }

    /** Only called once {@link #validate} passed, so the vital sign type code is known to be valid. */
    private boolean isDuplicate(DetectVitalSignsCommand command) {
        return command.measuredAt() != null && vitalSignRepository.existsByWearableDeviceIdAndVitalSignTypeAndMeasuredAt(
                new WearableDeviceId(command.wearableDeviceId()),
                VitalSignType.fromCode(command.vitalSignType()),
                command.measuredAt());
    }

    private static String detailsOf(ApplicationError error) {
        return error.details() != null ? error.details() : error.message();
    }

    private static String resolve(String key) {
        return MessageResolver.resolveOrDefault(key, key);
    }
}
