package com.healthify.guardian.platform.emergencyalerting.infrastructure.acl;

import com.healthify.guardian.platform.emergencyalerting.application.acl.ProfileContextAcl;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Temporary {@link ProfileContextAcl} used until the {@code Profile} bounded context exists.
 *
 * <p>Trusts every care relationship, consistent with how this platform treats cross-context
 * identifiers in the meantime. Replace it with an adapter over Profile's context facade once that
 * context is implemented.</p>
 */
@Component
public class ProfileContextAclStub implements ProfileContextAcl {

    @Override
    public boolean hasActiveCareRelationship(CareRecipientProfileId careRecipientProfileId, UserId userId) {
        return true;
    }

    @Override
    public Optional<String> findCareRecipientDisplayName(CareRecipientProfileId careRecipientProfileId) {
        return Optional.empty();
    }
}
