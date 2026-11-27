package com.healthify.guardian.platform.profile.application.acl;

import com.healthify.guardian.platform.profile.application.queryservices.CareRecipientProfileQueryService;
import com.healthify.guardian.platform.profile.application.queryservices.CareRelationshipQueryService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRecipientProfile;
import com.healthify.guardian.platform.profile.domain.model.aggregates.CareRelationship;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRecipientProfileQuery;
import com.healthify.guardian.platform.profile.domain.model.queries.GetCareRelationshipsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.CareRelationshipStatus;
import com.healthify.guardian.platform.profile.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests of {@link ProfileContextFacadeImpl}.
 */
class ProfileContextFacadeImplTest {

    private final CareRelationshipQueryService careRelationshipQueryService = mock(CareRelationshipQueryService.class);
    private final CareRecipientProfileQueryService careRecipientProfileQueryService =
            mock(CareRecipientProfileQueryService.class);
    private final ProfileContextFacadeImpl facade =
            new ProfileContextFacadeImpl(careRelationshipQueryService, careRecipientProfileQueryService);

    private final UUID careRecipient = UUID.randomUUID();

    @Test
    void theCaregiversAreTheUsersWithAnActiveCareRelationship() {
        var caregiver = UUID.randomUUID();
        var formerCaregiver = UUID.randomUUID();
        givenRelationships(
                relationship(caregiver, CareRelationshipStatus.ACTIVE),
                relationship(formerCaregiver, CareRelationshipStatus.ENDED));

        assertThat(facade.fetchActiveCaregiverUserIds(careRecipient)).containsExactly(caregiver);
        assertThat(facade.hasActiveCareRelationship(careRecipient, caregiver)).isTrue();
        assertThat(facade.hasActiveCareRelationship(careRecipient, formerCaregiver)).isFalse();
    }

    @Test
    void aPersonWithoutCaregiversHasNone() {
        givenRelationships();

        assertThat(facade.fetchActiveCaregiverUserIds(careRecipient)).isEmpty();
        assertThat(facade.fetchActiveCaregiverUserIds(null)).isEmpty();
    }

    @Test
    void theDisplayNameJoinsFirstAndLastName() {
        var profile = new CareRecipientProfile();
        profile.setFirstName("Ana");
        profile.setLastName("Pérez");
        when(careRecipientProfileQueryService.handle(any(GetCareRecipientProfileQuery.class)))
                .thenReturn(Optional.of(profile));

        assertThat(facade.fetchCareRecipientDisplayName(careRecipient)).contains("Ana Pérez");
    }

    @Test
    void anUnknownProfileHasNoDisplayName() {
        when(careRecipientProfileQueryService.handle(any(GetCareRecipientProfileQuery.class)))
                .thenReturn(Optional.empty());

        assertThat(facade.fetchCareRecipientDisplayName(careRecipient)).isEmpty();
    }

    private void givenRelationships(CareRelationship... relationships) {
        when(careRelationshipQueryService.handle(any(GetCareRelationshipsByCareRecipientProfileIdQuery.class)))
                .thenReturn(List.of(relationships));
    }

    private CareRelationship relationship(UUID userId, CareRelationshipStatus status) {
        var relationship = new CareRelationship();
        relationship.setUserId(new UserId(userId));
        relationship.setCareRecipientProfileId(new CareRecipientProfileId(careRecipient));
        relationship.setStatus(status);
        return relationship;
    }
}
