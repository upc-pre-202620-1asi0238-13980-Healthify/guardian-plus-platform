package com.healthify.guardian.platform.emergencyalerting.application.commandservices;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.AddEmergencyContactCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.DeactivateEmergencyContactCommand;
import com.healthify.guardian.platform.emergencyalerting.domain.model.commands.ReorderEmergencyContactsCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

import java.util.List;

/**
 * Application service contract for commands over the {@code EmergencyContact} aggregate.
 *
 * <p>Every command leaves a Fragile Citizen's active contacts with consecutive priorities starting
 * at 1, so the primary contact is always the one with priority 1.</p>
 */
public interface EmergencyContactCommandService {

    /**
     * Registers a Care Circle member as an emergency contact, inserting them at the requested
     * priority (or last, if none is given) and shifting the following contacts down. A previously
     * deactivated contact for the same user is reactivated with the new details.
     *
     * @param command the contact data
     * @return the registered contact or an application error
     */
    Result<EmergencyContact, ApplicationError> handle(AddEmergencyContactCommand command);

    /**
     * Reassigns the priority of every active contact of a Fragile Citizen.
     *
     * @param command every active contact id, in the new order
     * @return the reordered contacts, primary first, or an application error
     */
    Result<List<EmergencyContact>, ApplicationError> handle(ReorderEmergencyContactsCommand command);

    /**
     * Deactivates a contact, rejecting the operation if it would leave the Fragile Citizen without
     * any active contact.
     *
     * @param command the contact to deactivate
     * @return the deactivated contact or an application error
     */
    Result<EmergencyContact, ApplicationError> handle(DeactivateEmergencyContactCommand command);
}
