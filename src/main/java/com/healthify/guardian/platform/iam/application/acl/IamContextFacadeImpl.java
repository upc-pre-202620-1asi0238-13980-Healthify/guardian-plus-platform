package com.healthify.guardian.platform.iam.application.acl;

import com.healthify.guardian.platform.iam.application.outboundservices.SessionTokenService;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import com.healthify.guardian.platform.iam.interfaces.acl.IamContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the public facade of the IAM bounded context.
 */
@Service
public class IamContextFacadeImpl implements IamContextFacade {

    private final UserAccountRepository userAccountRepository;
    private final SessionTokenService sessionTokenService;

    public IamContextFacadeImpl(UserAccountRepository userAccountRepository, SessionTokenService sessionTokenService) {
        this.userAccountRepository = userAccountRepository;
        this.sessionTokenService = sessionTokenService;
    }

    @Override
    public boolean existsUserAccount(UUID userAccountId) {
        return userAccountId != null && userAccountRepository.findById(new UserAccountId(userAccountId)).isPresent();
    }

    @Override
    public Optional<String> fetchEmailByUserAccountId(UUID userAccountId) {
        if (userAccountId == null) {
            return Optional.empty();
        }
        return userAccountRepository.findById(new UserAccountId(userAccountId)).map(account -> account.getEmail().value());
    }

    @Override
    public Optional<UUID> fetchUserAccountIdBySessionToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return Optional.empty();
        }
        return sessionTokenService.resolveUserAccountId(accessToken).map(UserAccountId::value);
    }
}
