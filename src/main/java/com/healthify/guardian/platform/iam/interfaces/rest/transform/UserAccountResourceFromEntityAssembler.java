package com.healthify.guardian.platform.iam.interfaces.rest.transform;

import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.interfaces.rest.resources.UserAccountResource;

/**
 * Assembler converting a {@link UserAccount} aggregate into a {@link UserAccountResource}.
 */
public final class UserAccountResourceFromEntityAssembler {

    private UserAccountResourceFromEntityAssembler() {
    }

    public static UserAccountResource toResourceFromEntity(UserAccount account) {
        return new UserAccountResource(
                account.getId().value(),
                account.getEmail().value(),
                account.getStatus().name(),
                account.isEmailVerified());
    }
}
