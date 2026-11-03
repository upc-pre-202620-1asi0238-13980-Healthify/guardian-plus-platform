package com.healthify.guardian.platform.emergencyalerting.domain.services;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.RecipientLevel;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import org.springframework.stereotype.Service;

/**
 * Decides at which level of the escalation chain a confirmed alert is first dispatched
 * (<em>Dispatch Strategy Selector</em>).
 */
@Service
public class DispatchStrategyPolicy {

    /**
     * Resolves the initial recipient level of a confirmed alert.
     *
     * <ul>
     *   <li>{@code CRITICAL} reaches every active contact at once when the Fragile Citizen's settings
     *       ask for it; otherwise it starts with the primary contact and escalates.</li>
     *   <li>{@code CRITICAL} and {@code HIGH} reach every active contact at once when escalation is
     *       disabled, since nobody would otherwise be notified after the primary contact.</li>
     *   <li>Otherwise, and always for {@code MEDIUM}, only the primary contact is notified.</li>
     * </ul>
     *
     * @param severity the alert's severity
     * @param settings the Fragile Citizen's alerting configuration
     * @return the level to dispatch the alert at first
     */
    public RecipientLevel resolveInitialLevel(Severity severity, AlertSettings settings) {
        if (severity.allowsImmediateBroadcast() && settings.isBroadcastCriticalImmediately()) {
            return RecipientLevel.BROADCAST;
        }
        if (severity.allowsEscalation() && !settings.isEscalationEnabled()) {
            return RecipientLevel.BROADCAST;
        }
        return RecipientLevel.PRIMARY;
    }
}
