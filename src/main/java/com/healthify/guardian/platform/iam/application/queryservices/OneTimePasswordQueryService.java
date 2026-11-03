package com.healthify.guardian.platform.iam.application.queryservices;

import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.queries.GetUsableOtpByUserAccountIdAndPurposeQuery;

import java.util.Optional;

/**
 * Application service contract for one-time password read queries.
 */
public interface OneTimePasswordQueryService {

    Optional<OneTimePassword> handle(GetUsableOtpByUserAccountIdAndPurposeQuery query);
}
