package com.healthify.guardian.platform.iam.interfaces.acl;

import java.util.Optional;
import java.util.UUID;

/**
 * Public facade of the IAM bounded context for synchronous queries from other contexts. It only exposes primitives,
 * never the credentials model.
 */
public interface IamContextFacade {

    /**
     * Whether a user account exists.
     */
    boolean existsUserAccount(UUID userAccountId);

    /**
     * E-mail of a user account; empty if it does not exist.
     */
    Optional<String> fetchEmailByUserAccountId(UUID userAccountId);

    /**
     * Validates a session token (JWT) and returns the account it belongs to; empty if it is invalid or expired.
     */
    Optional<UUID> fetchUserAccountIdBySessionToken(String accessToken);
}
