package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to trigger a new alert from a risk signal on a Fragile Citizen.
 *
 * <p>Severity is not part of the command: it is derived from the source type by the domain.</p>
 *
 * @param careRecipientProfileId the Fragile Citizen the signal belongs to
 * @param sourceType             the kind of signal that raised the alert
 * @param sourceReferenceId      the record, in the supplier context, that originated the signal
 * @param triggeredAt            when the signal was detected
 */
public record TriggerAlertCommand(
        UUID careRecipientProfileId,
        AlertSourceType sourceType,
        UUID sourceReferenceId,
        Instant triggeredAt) {
}
