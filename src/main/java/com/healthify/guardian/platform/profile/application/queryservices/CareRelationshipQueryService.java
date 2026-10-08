package com.healthify.guardian.platform.profile.application.queryservices;

import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRelationshipsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRelationshipsByUserIdQuery;

import java.util.List;

/**
 * Application service contract for care relationship queries.
 */
public interface CareRelationshipQueryService {

    List<CareRelationship> handle(
            GetCareRelationshipsByUserIdQuery query);

    List<CareRelationship> handle(
            GetCareRelationshipsByCareRecipientProfileIdQuery query);
}