package com.healthify.guardian.platform.profile.application.internal.queryservices;

import com.healthify.guardian.platform.profile.application.queryservices.UserProfileQueryService;
import com.healthify.guardian.platform.profile.domain.model.aggregates.UserProfile;
import com.healthify.guardian.platform.profile.domain.model.queries.GetUserProfileByUserIdQuery;
import com.healthify.guardian.platform.profile.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that answers user profile queries.
 */
@Service
public class UserProfileQueryServiceImpl implements UserProfileQueryService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileQueryServiceImpl(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public Optional<UserProfile> handle(GetUserProfileByUserIdQuery query) {
        return userProfileRepository.findByUserId(query.userId());
    }
}