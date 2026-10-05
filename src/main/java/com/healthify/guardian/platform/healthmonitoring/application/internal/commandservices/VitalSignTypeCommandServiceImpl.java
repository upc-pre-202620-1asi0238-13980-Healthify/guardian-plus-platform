package com.healthify.guardian.platform.healthmonitoring.application.internal.commandservices;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignTypeCommandService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.repositories.VitalSignTypeRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

/**
 * Application service that executes vital sign type catalog commands.
 */
@Service
public class VitalSignTypeCommandServiceImpl implements VitalSignTypeCommandService {

    private static final String CODE_TAKEN_KEY = "vital-sign-type.code.taken";

    private final VitalSignTypeRepository vitalSignTypeRepository;

    public VitalSignTypeCommandServiceImpl(VitalSignTypeRepository vitalSignTypeRepository) {
        this.vitalSignTypeRepository = vitalSignTypeRepository;
    }

    @Override
    public Result<VitalSignType, ApplicationError> handle(RegisterVitalSignTypeCommand command) {
        try {
            var vitalSignType = new VitalSignType(command);
            if (vitalSignTypeRepository.findByCode(vitalSignType.getCode()).isPresent()) {
                return Result.failure(ApplicationError.conflict("VitalSignType",
                        MessageResolver.resolveOrDefault(CODE_TAKEN_KEY, CODE_TAKEN_KEY)));
            }
            return Result.success(vitalSignTypeRepository.save(vitalSignType));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "register-vital-sign-type", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }
}
