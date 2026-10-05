package com.healthify.guardian.platform.healthmonitoring.application.commandservices;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSign;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.DetectVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EmitVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EvaluateVitalSignsThresholdsCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

import java.util.List;

/**
 * Application service contract for commands over the {@code VitalSign} aggregate.
 */
public interface VitalSignCommandService {

    /**
     * Detects a single reading sent by an assigned wearable device. The Detect, Emit and Evaluate
     * steps then follow through domain events.
     *
     * @param command the reading
     * @return the stored vital sign, in its latest state, or an application error
     */
    Result<VitalSign, ApplicationError> handle(DetectVitalSignsCommand command);

    /**
     * Detects a batch of readings synchronized from the offline buffer of a wearable (TS02, US21).
     * The batch is validated as a whole before anything is stored; readings already stored are skipped.
     *
     * @param commands the readings of the batch
     * @return the newly stored vital signs, or an application error describing the first invalid item
     */
    Result<List<VitalSign>, ApplicationError> handleBatch(List<DetectVitalSignsCommand> commands);

    /**
     * Publishes a detected reading for live consumption.
     */
    Result<VitalSign, ApplicationError> handle(EmitVitalSignsCommand command);

    /**
     * Evaluates an emitted reading against the active threshold of its care recipient and type.
     * When no active threshold exists the reading is left unevaluated.
     */
    Result<VitalSign, ApplicationError> handle(EvaluateVitalSignsThresholdsCommand command);
}
