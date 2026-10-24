package com.healthify.guardian.platform.iam.application.internal.queryservices;

import com.healthify.guardian.platform.iam.application.queryservices.UserAccountQueryService;
import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.queries.GetEmailAvailabilityQuery;
import com.healthify.guardian.platform.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.healthify.guardian.platform.iam.domain.model.queries.GetUserAccountByIdQuery;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that resolves user account read queries.
 */
@Service
public class UserAccountQueryServiceImpl implements UserAccountQueryService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountQueryServiceImpl(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public Optional<UserAccount> handle(GetUserAccountByIdQuery query) {
        return userAccountRepository.findById(query.userAccountId());
    }

    @Override
    public Optional<UserAccount> handle(GetUserAccountByEmailQuery query) {
        return userAccountRepository.findByEmail(query.email());
    }

    @Override
    public boolean handle(GetEmailAvailabilityQuery query) {
        return !userAccountRepository.existsByEmail(new Email(query.email()));
    }
}
