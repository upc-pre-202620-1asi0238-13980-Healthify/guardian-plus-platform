package com.healthify.guardian.platform.emergencyalerting.application.commandservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Incident;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.CloseIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.OpenIncidentCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.StabilizeIncidentCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code Incident} aggregate.
 */
public interface IncidentCommandService {

    /**
     * Opens the incident of an acknowledged alert. An alert originates at most one incident.
     *
     * @param command the acknowledged alert
     * @return the opened incident or an application error
     */
    Result<Incident, ApplicationError> handle(OpenIncidentCommand command);

    /**
     * Declares the Fragile Citizen's situation stabilized.
     *
     * @param command the incident to stabilize
     * @return the updated incident or an application error
     */
    Result<Incident, ApplicationError> handle(StabilizeIncidentCommand command);

    /**
     * Definitively closes an incident, which in turn resolves its alert.
     *
     * @param command the incident to close
     * @return the updated incident or an application error
     */
    Result<Incident, ApplicationError> handle(CloseIncidentCommand command);
}
