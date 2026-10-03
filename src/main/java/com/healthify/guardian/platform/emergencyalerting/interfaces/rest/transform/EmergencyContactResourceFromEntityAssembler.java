package com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.EmergencyContactResource;

/**
 * Assembler that converts an {@link EmergencyContact} domain aggregate into an {@link EmergencyContactResource}.
 */
public final class EmergencyContactResourceFromEntityAssembler {

    private EmergencyContactResourceFromEntityAssembler() {
    }

    public static EmergencyContactResource toResourceFromEntity(EmergencyContact contact) {
        return new EmergencyContactResource(
                contact.getId().value(),
                contact.getCareRecipientProfileId().value(),
                contact.getUserId().value(),
                contact.getDisplayName(),
                contact.getRelationship(),
                contact.getPhoneNumber().value(),
                contact.getPriorityOrder().value(),
                contact.isActive());
    }
}
