package com.healthify.guardian.platform.emergencyalerting.domain.model.commands;

import java.util.List;
import java.util.UUID;

/**
 * Command to reassign the escalation priority of a Fragile Citizen's active emergency contacts.
 *
 * @param careRecipientProfileId     the Fragile Citizen whose contacts are reordered
 * @param orderedEmergencyContactIds every active contact id, in the new priority order
 */
public record ReorderEmergencyContactsCommand(UUID careRecipientProfileId, List<UUID> orderedEmergencyContactIds) {
}
