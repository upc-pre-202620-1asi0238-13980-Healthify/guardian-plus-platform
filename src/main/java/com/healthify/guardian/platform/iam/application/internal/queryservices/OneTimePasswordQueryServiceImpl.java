package com.healthify.guardian.platform.iam.application.internal.queryservices;

import com.healthify.guardian.platform.iam.application.queryservices.OneTimePasswordQueryService;
import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.queries.GetUsableOtpByUserAccountIdAndPurposeQuery;
import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Application service that resolves one-time password read queries.
 */
@Service
public class OneTimePasswordQueryServiceImpl implements OneTimePasswordQueryService {

    private final OneTimePasswordRepository oneTimePasswordRepository;

    public OneTimePasswordQueryServiceImpl(OneTimePasswordRepository oneTimePasswordRepository) {
        this.oneTimePasswordRepository = oneTimePasswordRepository;
    }

    @Override
    public Optional<OneTimePassword> handle(GetUsableOtpByUserAccountIdAndPurposeQuery query) {
        return oneTimePasswordRepository.findLatestUsableByUserAccountIdAndPurpose(
                query.userAccountId(), query.purpose(), Instant.now());
    }
}
