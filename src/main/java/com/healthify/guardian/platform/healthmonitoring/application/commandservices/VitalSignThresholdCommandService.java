package com.healthify.guardian.platform.healthmonitoring.application.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignThreshold;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.ActivateVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DeactivateVitalSignThresholdCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DefineVitalSignThresholdCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code VitalSignThreshold} aggregate.
 */
public interface VitalSignThresholdCommandService {

    /**
     * Defines the threshold of a care recipient and type, or redefines the existing one.
     */
    Result<VitalSignThreshold, ApplicationError> handle(DefineVitalSignThresholdCommand command);

    Result<VitalSignThreshold, ApplicationError> handle(ActivateVitalSignThresholdCommand command);

    Result<VitalSignThreshold, ApplicationError> handle(DeactivateVitalSignThresholdCommand command);
}
