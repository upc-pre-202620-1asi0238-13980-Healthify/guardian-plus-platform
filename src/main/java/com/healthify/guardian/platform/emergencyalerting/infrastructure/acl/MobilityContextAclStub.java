package com.healthify.guardian.platform.emergencyalerting.infrastructure.acl;

import com.healthify.guardian.platform.emergencyalerting.application.acl.MobilityContextAcl;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Temporary {@link MobilityContextAcl} used until the {@code Mobility & Geofencing} bounded context
 * exists: no location is known, so notifications are sent without one. Replace it with an adapter
 * over that context's facade once it is implemented.
 */
@Component
public class MobilityContextAclStub implements MobilityContextAcl {

    @Override
    public Optional<LastKnownLocation> findLastKnownLocation(CareRecipientProfileId careRecipientProfileId) {
        return Optional.empty();
    }
}
