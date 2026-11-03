package com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedPassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.entities.UserAccountPersistenceEntity;

import java.time.Instant;
import java.util.Date;

/**
 * Static assembler between the {@link UserAccount} aggregate and its persistence entity.
 */
public final class UserAccountPersistenceAssembler {

    private UserAccountPersistenceAssembler() {
    }

    public static UserAccount toDomainFromPersistence(UserAccountPersistenceEntity entity) {
        if (entity == null) return null;
        var account = new UserAccount();
        account.setId(new UserAccountId(entity.getId()));
        account.setEmail(new Email(entity.getEmail()));
        account.setPasswordHash(new HashedPassword(entity.getPasswordHash()));
        account.setEmailVerified(entity.isEmailVerified());
        account.setStatus(entity.getStatus());
        account.setPasswordResetTokenHash(entity.getPasswordResetTokenHash() == null
                ? null : new HashedSecret(entity.getPasswordResetTokenHash()));
        account.setPasswordResetTokenExpiresAt(entity.getPasswordResetTokenExpiresAt());
        account.setCreatedAt(toInstant(entity.getCreatedAt()));
        account.setUpdatedAt(toInstant(entity.getUpdatedAt()));
        return account;
    }

    public static UserAccountPersistenceEntity toPersistenceFromDomain(UserAccount account) {
        if (account == null) return null;
        var entity = new UserAccountPersistenceEntity();
        entity.setId(account.getId().value());
        entity.setEmail(account.getEmail().value());
        entity.setPasswordHash(account.getPasswordHash().hash());
        entity.setEmailVerified(account.isEmailVerified());
        entity.setStatus(account.getStatus());
        entity.setPasswordResetTokenHash(account.getPasswordResetTokenHash() == null
                ? null : account.getPasswordResetTokenHash().hash());
        entity.setPasswordResetTokenExpiresAt(account.getPasswordResetTokenExpiresAt());
        return entity;
    }

    private static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
