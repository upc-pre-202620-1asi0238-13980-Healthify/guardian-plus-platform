package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


/**
 * Request payload to update a Fragile Citizen's alerting configuration.
 *
 * @param primaryAckTimeoutSec         seconds to wait for an acknowledgement before escalating (15 to 300)
 * @param escalationEnabled            whether unacknowledged alerts escalate to secondary contacts
 * @param broadcastCriticalImmediately whether critical alerts reach every contact at once
 */
public record UpdateAlertSettingsResource(
        @NotNull(message = "{alert-settings.ack-timeout.blank}")
        @Min(value = 15, message = "{ack-timeout.out-of-range}")
        @Max(value = 300, message = "{ack-timeout.out-of-range}")
        Integer primaryAckTimeoutSec,

        @NotNull(message = "{alert-settings.escalation-enabled.blank}")
        Boolean escalationEnabled,

        @NotNull(message = "{alert-settings.broadcast-critical.blank}")
        Boolean broadcastCriticalImmediately
) {
}
