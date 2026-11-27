package com.healthify.guardian.platform.profile.interfaces.acl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public facade of the Profile bounded context for synchronous queries from other contexts. It only exposes
 * primitives, never the profile model.
 */
public interface ProfileContextFacade {

    /**
     * Whether a user currently holds an active care relationship with a person under care.
     */
    boolean hasActiveCareRelationship(UUID careRecipientProfileId, UUID userId);

    /**
     * Users that currently hold an active care relationship with a person under care, i.e. their caregivers;
     * empty if there are none.
     */
    List<UUID> fetchActiveCaregiverUserIds(UUID careRecipientProfileId);

    /**
     * Name of a person under care, as their caregivers see it; empty if the profile does not exist.
     */
    Optional<String> fetchCareRecipientDisplayName(UUID careRecipientProfileId);
}
