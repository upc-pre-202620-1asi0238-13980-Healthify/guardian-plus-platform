package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.UUID;

/**
 * Command to update a Fragile Citizen's alerting configuration.
 *
 * @param careRecipientProfileId       the Fragile Citizen whose settings are updated
 * @param primaryAckTimeoutSec         seconds to wait for an acknowledgement before escalating
 * @param escalationEnabled            whether unacknowledged alerts escalate to secondary contacts
 * @param broadcastCriticalImmediately whether critical alerts skip escalation and reach every contact at once
 */
public record UpdateAlertSettingsCommand(
        UUID careRecipientProfileId,
        Integer primaryAckTimeoutSec,
        Boolean escalationEnabled,
        Boolean broadcastCriticalImmediately) {
}
