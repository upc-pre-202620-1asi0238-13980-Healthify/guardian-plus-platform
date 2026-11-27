package com.healthify.guardian.platform.profile.application.acl;

import com.healthify.guardian.platform.profile.application.queryservices.CareRecipientProfileQueryService;
import com.healthify.guardian.platform.profile.application.queryservices.CareRelationshipQueryService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRecipientProfileQuery;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRelationshipsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.interfaces.acl.ProfileContextFacade;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the public facade of the Profile bounded context.
 */
@Service
public class ProfileContextFacadeImpl implements ProfileContextFacade {

    private final CareRelationshipQueryService careRelationshipQueryService;
    private final CareRecipientProfileQueryService careRecipientProfileQueryService;

    public ProfileContextFacadeImpl(
            CareRelationshipQueryService careRelationshipQueryService,
            CareRecipientProfileQueryService careRecipientProfileQueryService) {
        this.careRelationshipQueryService = careRelationshipQueryService;
        this.careRecipientProfileQueryService = careRecipientProfileQueryService;
    }

    @Override
    public boolean hasActiveCareRelationship(UUID careRecipientProfileId, UUID userId) {
        return userId != null && fetchActiveCaregiverUserIds(careRecipientProfileId).contains(userId);
    }

    @Override
    public List<UUID> fetchActiveCaregiverUserIds(UUID careRecipientProfileId) {
        if (careRecipientProfileId == null) {
            return List.of();
        }
        return careRelationshipQueryService
                .handle(new GetCareRelationshipsByCareRecipientProfileIdQuery(new CareRecipientProfileId(careRecipientProfileId)))
                .stream()
                .filter(CareRelationship::isActive)
                .map(relationship -> relationship.getUserId().value())
                .distinct()
                .toList();
    }

    @Override
    public Optional<String> fetchCareRecipientDisplayName(UUID careRecipientProfileId) {
        if (careRecipientProfileId == null) {
            return Optional.empty();
        }
        return careRecipientProfileQueryService
                .handle(new GetCareRecipientProfileQuery(new CareRecipientProfileId(careRecipientProfileId)))
                .map(profile -> Stream.of(profile.getFirstName(), profile.getLastName())
                        .filter(part -> part != null && !part.isBlank())
                        .map(String::strip)
                        .collect(Collectors.joining(" ")))
                .filter(name -> !name.isBlank());
    }
}
