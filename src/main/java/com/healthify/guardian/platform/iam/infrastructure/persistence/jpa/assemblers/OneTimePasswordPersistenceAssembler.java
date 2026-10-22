package com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.entities.OneTimePasswordPersistenceEntity;

import java.time.Instant;
import java.util.Date;

/**
 * Static assembler between the {@link OneTimePassword} aggregate and its persistence entity.
 */
public final class OneTimePasswordPersistenceAssembler {

    private OneTimePasswordPersistenceAssembler() {
    }

    public static OneTimePassword toDomainFromPersistence(OneTimePasswordPersistenceEntity entity) {
        if (entity == null) return null;
        var otp = new OneTimePassword();
        otp.setId(new OtpId(entity.getId()));
        otp.setUserAccountId(new UserAccountId(entity.getUserAccountId()));
        otp.setCodeHash(new HashedSecret(entity.getCodeHash()));
        otp.setPurpose(entity.getPurpose());
        otp.setExpiresAt(entity.getExpiresAt());
        otp.setUsedAt(entity.getUsedAt());
        otp.setFailedAttempts(entity.getFailedAttempts());
        otp.setCreatedAt(toInstant(entity.getCreatedAt()));
        return otp;
    }

    public static OneTimePasswordPersistenceEntity toPersistenceFromDomain(OneTimePassword otp) {
        if (otp == null) return null;
        var entity = new OneTimePasswordPersistenceEntity();
        entity.setId(otp.getId().value());
        entity.setUserAccountId(otp.getUserAccountId().value());
        entity.setCodeHash(otp.getCodeHash().hash());
        entity.setPurpose(otp.getPurpose());
        entity.setExpiresAt(otp.getExpiresAt());
        entity.setUsedAt(otp.getUsedAt());
        entity.setFailedAttempts(otp.getFailedAttempts());
        return entity;
    }

    private static Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }
}
