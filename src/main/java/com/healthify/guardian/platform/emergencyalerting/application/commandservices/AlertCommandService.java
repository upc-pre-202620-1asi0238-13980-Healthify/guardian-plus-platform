package com.healthify.guardian.platform.emergencyalerting.application.commandservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.AcknowledgeAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.BroadcastAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ClaimAlertResponseCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.CompleteAlertResponseCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ConfirmAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DismissAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DispatchAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.EscalateAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.RegisterDeliveryResultCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ResolveAlertCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.TriggerAlertCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code Alert} aggregate.
 */
public interface AlertCommandService {

    /**
     * Triggers a new alert, or returns the alert already active for the same source so that a
     * retried signal never raises the same alert twice.
     *
     * @param command the signal data
     * @return the triggered (or already active) alert, or an application error
     */
    Result<Alert, ApplicationError> handle(TriggerAlertCommand command);

    /**
     * Confirms an alert, after its fall confirmation window or right away.
     *
     * @param command the alert to confirm
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(ConfirmAlertCommand command);

    /**
     * Dismisses a fall alert as a false positive within its confirmation window.
     *
     * @param command the alert to dismiss
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(DismissAlertCommand command);

    /**
     * Dispatches a confirmed alert at the initial level chosen by {@code DispatchStrategyPolicy}.
     *
     * @param command the alert to dispatch
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(DispatchAlertCommand command);

    /**
     * Escalates an unacknowledged alert to the secondary emergency contacts.
     *
     * @param command the alert to escalate
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(EscalateAlertCommand command);

    /**
     * Broadcasts an unacknowledged alert to every active emergency contact.
     *
     * @param command the alert to broadcast
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(BroadcastAlertCommand command);

    /**
     * Records the outcome a notification provider reported for a delivery.
     *
     * @param command the delivery outcome
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(RegisterDeliveryResultCommand command);

    /**
     * Records a recipient's acknowledgement, stopping the escalation.
     *
     * @param command the acknowledgement
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(AcknowledgeAlertCommand command);

    /**
     * Records that a recipient takes charge of the response.
     *
     * @param command the claim
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(ClaimAlertResponseCommand command);

    /**
     * Records the outcome of a claimed intervention.
     *
     * @param command the outcome
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(CompleteAlertResponseCommand command);

    /**
     * Definitively closes an alert once its incident is closed.
     *
     * @param command the alert to resolve
     * @return the updated alert or an application error
     */
    Result<Alert, ApplicationError> handle(ResolveAlertCommand command);
}
