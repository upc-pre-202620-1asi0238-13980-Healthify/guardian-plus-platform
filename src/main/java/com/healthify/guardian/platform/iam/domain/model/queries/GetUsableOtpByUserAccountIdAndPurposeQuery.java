package com.healthify.guardian.platform.iam.domain.model.queries;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;

/**
 * Query for the latest one-time code of an account that is still usable for the given purpose.
 */
public record GetUsableOtpByUserAccountIdAndPurposeQuery(UserAccountId userAccountId, OtpPurpose purpose) {
}
