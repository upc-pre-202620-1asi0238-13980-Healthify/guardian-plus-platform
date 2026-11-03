package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.AlertSettings;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.AlertSettingsResource;

/**
 * Assembler that converts an {@link AlertSettings} domain aggregate into an {@link AlertSettingsResource}.
 */
public final class AlertSettingsResourceFromEntityAssembler {

    private AlertSettingsResourceFromEntityAssembler() {
    }

    public static AlertSettingsResource toResourceFromEntity(AlertSettings settings) {
        return new AlertSettingsResource(
                settings.getCareRecipientProfileId().value(),
                settings.getPrimaryAckTimeout().seconds(),
                settings.isEscalationEnabled(),
                settings.isSilentModeEnabled(),
                settings.isBroadcastCriticalImmediately());
    }
}
