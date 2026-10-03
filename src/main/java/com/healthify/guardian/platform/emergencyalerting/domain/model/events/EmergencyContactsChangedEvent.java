package com.healthify.guardian.platform.emergencyalerting.domain.model.events;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.EmergencyContactChange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.EmergencyContactId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;


/**
 * Raised when an emergency contact is added, updated, reprioritized, activated or deactivated.
 */
public record EmergencyContactsChangedEvent(
        CareRecipientProfileId careRecipientProfileId,
        EmergencyContactId emergencyContactId,
        UserId userId,
        EmergencyContactChange change) {

    public static EmergencyContactsChangedEvent from(EmergencyContact contact, EmergencyContactChange change) {
        return new EmergencyContactsChangedEvent(
                contact.getCareRecipientProfileId(), contact.getId(), contact.getUserId(), change);
    }
}
