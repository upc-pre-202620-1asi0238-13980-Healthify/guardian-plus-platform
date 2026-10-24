package com.healthify.guardian.platform.profile.domain.repositories;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the {@link CareRecipientProfile} aggregate.
 */
public interface CareRecipientProfileRepository {

    CareRecipientProfile save(CareRecipientProfile profile);

    Optional<CareRecipientProfile> findById(CareRecipientProfileId id);

    List<CareRecipientProfile> findByCreatedByUserId(UserId userId);
}