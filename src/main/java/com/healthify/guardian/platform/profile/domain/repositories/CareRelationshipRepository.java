package com.healthify.guardian.platform.profile.domain.repositories;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the {@link CareRelationship} aggregate.
 */
public interface CareRelationshipRepository {

    CareRelationship save(CareRelationship relationship);

    Optional<CareRelationship> findById(CareRelationshipId id);

    List<CareRelationship> findActiveByUserId(UserId userId);

    List<CareRelationship> findActiveByCareRecipientId(
            CareRecipientProfileId careRecipientProfileId);
}