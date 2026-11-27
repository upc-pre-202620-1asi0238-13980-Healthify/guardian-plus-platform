package com.healthify.guardian.platform.emergencyalerting.infrastructure.acl;

import com.healthify.guardian.platform.emergencyalerting.application.acl.ProfileContextAcl;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.profile.interfaces.acl.ProfileContextFacade;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * {@link ProfileContextAcl} over the public facade of the {@code Profile} bounded context: translates its
 * primitives into this context's value objects.
 */
@Component
public class ProfileContextAclAdapter implements ProfileContextAcl {

    private final ProfileContextFacade profileContextFacade;

    public ProfileContextAclAdapter(ProfileContextFacade profileContextFacade) {
        this.profileContextFacade = profileContextFacade;
    }

    @Override
    public boolean hasActiveCareRelationship(CareRecipientProfileId careRecipientProfileId, UserId userId) {
        return profileContextFacade.hasActiveCareRelationship(careRecipientProfileId.value(), userId.value());
    }

    @Override
    public List<UserId> findActiveCaregivers(CareRecipientProfileId careRecipientProfileId) {
        return profileContextFacade.fetchActiveCaregiverUserIds(careRecipientProfileId.value()).stream()
                .map(UserId::new)
                .toList();
    }

    @Override
    public Optional<String> findCareRecipientDisplayName(CareRecipientProfileId careRecipientProfileId) {
        return profileContextFacade.fetchCareRecipientDisplayName(careRecipientProfileId.value());
    }
}
