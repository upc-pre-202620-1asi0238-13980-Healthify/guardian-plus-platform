package com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.iam.domain.model.aggregates.OneTimePassword;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpId;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.repositories.OneTimePasswordRepository;
import com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.assemblers.OneTimePasswordPersistenceAssembler;
import com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.repositories.OneTimePasswordPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * JPA adapter for the {@link OneTimePasswordRepository} domain port.
 */
@Repository
public class OneTimePasswordRepositoryImpl implements OneTimePasswordRepository {

    private final OneTimePasswordPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public OneTimePasswordRepositoryImpl(OneTimePasswordPersistenceRepository persistenceRepository,
                                         ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public OneTimePassword save(OneTimePassword otp) {
        var savedEntity = persistenceRepository.save(OneTimePasswordPersistenceAssembler.toPersistenceFromDomain(otp));
        otp.domainEvents().forEach(eventPublisher::publishEvent);
        otp.clearDomainEvents();
        return OneTimePasswordPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public Optional<OneTimePassword> findById(OtpId id) {
        return persistenceRepository.findById(id.value()).map(OneTimePasswordPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<OneTimePassword> findLatestUsableByUserAccountIdAndPurpose(UserAccountId userAccountId,
                                                                              OtpPurpose purpose, Instant now) {
        return persistenceRepository.findUsable(userAccountId.value(), purpose, now,
                        OneTimePassword.MAX_FAILED_ATTEMPTS, Limit.of(1))
                .stream().findFirst()
                .map(OneTimePasswordPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public int countIssuedSince(UserAccountId userAccountId, OtpPurpose purpose, Instant since) {
        return persistenceRepository.countByUserAccountIdAndPurposeAndCreatedAtAfter(
                userAccountId.value(), purpose, Date.from(since));
    }

    @Override
    public int deleteExpiredUnusedBefore(Instant threshold) {
        return persistenceRepository.deleteExpiredUnusedBefore(threshold);
    }
}
