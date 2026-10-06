package com.healthify.guardian.platform.profile.interfaces.rest.transform;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.interfaces.rest.resources.CareRecipientProfileResource;

/**
 * Assembler that converts a {@link CareRecipientProfile}
 * aggregate into a {@link CareRecipientProfileResource}.
 */
public final class CareRecipientProfileResourceFromEntityAssembler {

    private CareRecipientProfileResourceFromEntityAssembler() {
    }

    public static CareRecipientProfileResource toResourceFromEntity(
            CareRecipientProfile profile) {

        return new CareRecipientProfileResource(
                profile.getId().value(),
                profile.getCreatedByUserId().value(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getBirthDate(),
                profile.getProfileImageUrl(),
                profile.getCreatedAt(),
                profile.getUpdatedAt());
    }
}