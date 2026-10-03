package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import java.util.UUID;

/**
 * Response payload representing a Fragile Citizen's alerting configuration.
 *
 * @param careRecipientProfileId       the Fragile Citizen
 * @param primaryAckTimeoutSec         seconds to wait for an acknowledgement before escalating
 * @param escalationEnabled            whether unacknowledged alerts escalate
 * @param silentModeEnabled            whether only critical alerts may be audible
 * @param broadcastCriticalImmediately whether critical alerts reach every contact at once
 */
public record AlertSettingsResource(
        UUID careRecipientProfileId,
        Integer primaryAckTimeoutSec,
        boolean escalationEnabled,
        boolean silentModeEnabled,
        boolean broadcastCriticalImmediately
) {
}
