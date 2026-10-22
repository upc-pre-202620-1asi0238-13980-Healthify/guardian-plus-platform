package com.healthify.guardian.platform.iam.domain.repositories;

import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

import java.time.Instant;
import java.util.Optional;

/**
 * One-time password aggregate repository port.
 */
public interface OneTimePasswordRepository {

    OneTimePassword save(OneTimePassword otp);

    Optional<OneTimePassword> findById(OtpId id);

    /**
     * Retrieves the most recently issued code of an account for the given purpose that is neither used, expired
     * nor locked by failed attempts at {@code now}.
     */
    Optional<OneTimePassword> findLatestUsableByUserAccountIdAndPurpose(UserAccountId userAccountId,
                                                                       OtpPurpose purpose, Instant now);

    /**
     * Counts the codes issued to an account for the given purpose since {@code since}, used or not, so issuing
     * can be rate limited.
     */
    int countIssuedSince(UserAccountId userAccountId, OtpPurpose purpose, Instant since);

    /**
     * Deletes the codes that expired before {@code threshold} without being used.
     *
     * @return how many codes were deleted
     */
    int deleteExpiredUnusedBefore(Instant threshold);
}
