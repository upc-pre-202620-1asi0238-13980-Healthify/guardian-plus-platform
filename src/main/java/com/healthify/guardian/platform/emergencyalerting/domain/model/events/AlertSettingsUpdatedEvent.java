package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AckTimeout;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSettingsId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;


/**
 * Raised when a Fragile Citizen's acknowledgement timeout, escalation or silent mode change.
 */
public record AlertSettingsUpdatedEvent(
        AlertSettingsId alertSettingsId,
        CareRecipientProfileId careRecipientProfileId,
        AckTimeout primaryAckTimeout,
        boolean escalationEnabled,
        boolean silentModeEnabled,
        boolean broadcastCriticalImmediately) {

    public static AlertSettingsUpdatedEvent from(AlertSettings settings) {
        return new AlertSettingsUpdatedEvent(
                settings.getId(), settings.getCareRecipientProfileId(), settings.getPrimaryAckTimeout(),
                settings.isEscalationEnabled(), settings.isSilentModeEnabled(),
                settings.isBroadcastCriticalImmediately());
    }
}
