package com.healthify.guardian.platform.iam.application.outboundservices;

import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.util.Optional;

/**
 * Outbound port that issues and validates the signed session tokens (JWT) other bounded contexts trust.
 */
public interface SessionTokenService {

    /**
     * Issues a session token for an authenticated account.
     */
    SessionToken issue(UserAccount account);

    /**
     * Validates a session token and returns the account it belongs to.
     *
     * @return the account id, or empty if the token is malformed, tampered with or expired
     */
    Optional<UserAccountId> resolveUserAccountId(String accessToken);
}
