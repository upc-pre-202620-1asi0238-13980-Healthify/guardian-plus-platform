package com.healthify.guardian.platform.profile.application.internal.queryservices;

import com.healthify.guardian.platform.profile.application.queryservices.CareRecipientProfileQueryService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRecipientProfileQuery;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRecipientProfilesByCreatedByUserIdQuery;
import com.healthify.guardian.platform.profile.domain.repositories.CareRecipientProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that answers care recipient profile queries.
 */
@Service
public class CareRecipientProfileQueryServiceImpl
        implements CareRecipientProfileQueryService {

    private final CareRecipientProfileRepository
            careRecipientProfileRepository;

    public CareRecipientProfileQueryServiceImpl(
            CareRecipientProfileRepository careRecipientProfileRepository) {

        this.careRecipientProfileRepository =
                careRecipientProfileRepository;
    }

    @Override
    public Optional<CareRecipientProfile> handle(
            GetCareRecipientProfileQuery query) {

        return careRecipientProfileRepository.findById(
                query.careRecipientProfileId());
    }

    @Override
    public List<CareRecipientProfile> handle(
            GetCareRecipientProfilesByCreatedByUserIdQuery query) {

        return careRecipientProfileRepository
                .findByCreatedByUserId(
                        query.createdByUserId());
    }
}