package com.healthify.guardian.platform.iam.application.queryservices;

import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.queries.GetEmailAvailabilityQuery;
import com.healthify.guardian.platform.iam.domain.model.queries.GetUserAccountByEmailQuery;
import com.healthify.guardian.platform.iam.domain.model.queries.GetUserAccountByIdQuery;

import java.util.Optional;

/**
 * Application service contract for user account read queries.
 */
public interface UserAccountQueryService {

    Optional<UserAccount> handle(GetUserAccountByIdQuery query);

    Optional<UserAccount> handle(GetUserAccountByEmailQuery query);

    /**
     * Whether the e-mail is still free to register.
     *
     * @throws IllegalArgumentException if the e-mail is not a valid address
     */
    boolean handle(GetEmailAvailabilityQuery query);
}
