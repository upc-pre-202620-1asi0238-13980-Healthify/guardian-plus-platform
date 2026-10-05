package com.healthify.guardian.platform.healthmonitoring.application.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.RegisterVitalSignTypeCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code VitalSignType} catalog.
 */
public interface VitalSignTypeCommandService {

    Result<VitalSignType, ApplicationError> handle(RegisterVitalSignTypeCommand command);
}
