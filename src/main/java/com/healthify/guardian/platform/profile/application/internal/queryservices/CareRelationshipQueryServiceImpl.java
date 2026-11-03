package com.healthify.guardian.platform.profile.application.internal.queryservices;

import com.healthify.guardian.platform.profile.application.queryservices.CareRelationshipQueryService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRelationshipsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRelationshipsByUserIdQuery;
import com.healthify.guardian.platform.profile.domain.repositories.CareRelationshipRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that executes care relationship queries.
 */
@Service
public class CareRelationshipQueryServiceImpl
        implements CareRelationshipQueryService {

    private final CareRelationshipRepository careRelationshipRepository;

    public CareRelationshipQueryServiceImpl(
            CareRelationshipRepository careRelationshipRepository) {
        this.careRelationshipRepository =
                careRelationshipRepository;
    }

    @Override
    public List<CareRelationship> handle(
            GetCareRelationshipsByUserIdQuery query) {

        return careRelationshipRepository
                .findActiveByUserId(query.userId());
    }

    @Override
    public List<CareRelationship> handle(
            GetCareRelationshipsByCareRecipientProfileIdQuery query) {

        return careRelationshipRepository
                .findActiveByCareRecipientId(
                        query.careRecipientProfileId());
    }
}