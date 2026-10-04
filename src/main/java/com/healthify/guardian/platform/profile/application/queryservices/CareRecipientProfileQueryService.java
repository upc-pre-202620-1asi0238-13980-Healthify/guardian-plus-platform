package com.healthify.guardian.platform.profile.application.queryservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRecipientProfileQuery;

import java.util.Optional;

/**
 * Application service contract for queries over the {@code CareRecipientProfile} aggregate.
 */
public interface CareRecipientProfileQueryService {

    Optional<CareRecipientProfile> handle(GetCareRecipientProfileQuery query);
}