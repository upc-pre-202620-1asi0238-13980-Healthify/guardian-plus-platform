package com.healthify.guardian.platform.emergencyalerting.domain.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.EmergencyContact;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.EmergencyContactId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * EmergencyContact aggregate repository port.
 */
public interface EmergencyContactRepository {

    /**
     * Persists an emergency contact and publishes its registered domain events.
     *
     * @param contact the contact to save
     * @return the saved contact
     */
    EmergencyContact save(EmergencyContact contact);

    /**
     * Persists several emergency contacts at once, e.g. after reordering them, and publishes their
     * registered domain events.
     *
     * @param contacts the contacts to save
     * @return the saved contacts
     */
    List<EmergencyContact> saveAll(List<EmergencyContact> contacts);

    Optional<EmergencyContact> findById(EmergencyContactId id);

    /**
     * Retrieves a Fragile Citizen's active emergency contacts.
     *
     * @param careRecipientProfileId the Fragile Citizen whose contacts are requested
     * @return the active contacts, primary contact first
     */
    List<EmergencyContact> findActiveByCareRecipientProfileIdOrderByPriority(CareRecipientProfileId careRecipientProfileId);

    Optional<EmergencyContact> findByCareRecipientProfileIdAndUserId(
            CareRecipientProfileId careRecipientProfileId, UserId userId);
}
