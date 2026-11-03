package com.healthify.guardian.platform.emergencyalerting.application.acl;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;

import java.util.Optional;

/**
 * Anti-corruption layer towards the {@code Profile} bounded context: the only questions this
 * context needs to ask about care relationships and care recipients, in its own language.
 */
public interface ProfileContextAcl {

    /**
     * Tells whether a Care Circle member currently holds an active care relationship with a
     * Fragile Citizen, i.e. may be registered as one of their emergency contacts.
     *
     * @param careRecipientProfileId the Fragile Citizen
     * @param userId                 the Care Circle member
     * @return true if the relationship exists and is active
     */
    boolean hasActiveCareRelationship(CareRecipientProfileId careRecipientProfileId, UserId userId);

    /**
     * Retrieves the Fragile Citizen's name, used to compose notification contents.
     *
     * @param careRecipientProfileId the Fragile Citizen
     * @return their display name, if known
     */
    Optional<String> findCareRecipientDisplayName(CareRecipientProfileId careRecipientProfileId);
}
