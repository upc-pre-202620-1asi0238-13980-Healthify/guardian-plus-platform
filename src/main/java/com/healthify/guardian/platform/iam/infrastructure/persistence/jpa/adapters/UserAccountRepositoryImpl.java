package com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.iam.domain.model.aggregates.UserAccount;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.Email;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.HashedSecret;
import com.healthify.guardian.platform.iam.domain.model.valueobjects.UserAccountId;
import com.healthify.guardian.platform.iam.domain.repositories.UserAccountRepository;
import com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.assemblers.UserAccountPersistenceAssembler;
import com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.repositories.UserAccountPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA adapter for the {@link UserAccountRepository} domain port.
 */
@Repository
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final UserAccountPersistenceRepository persistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserAccountRepositoryImpl(UserAccountPersistenceRepository persistenceRepository,
                                     ApplicationEventPublisher eventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserAccount save(UserAccount account) {
        var savedEntity = persistenceRepository.save(UserAccountPersistenceAssembler.toPersistenceFromDomain(account));
        account.domainEvents().forEach(eventPublisher::publishEvent);
        account.clearDomainEvents();
        return UserAccountPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }

    @Override
    public Optional<UserAccount> findById(UserAccountId id) {
        return persistenceRepository.findById(id.value()).map(UserAccountPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<UserAccount> findByEmail(Email email) {
        return persistenceRepository.findByEmail(email.value()).map(UserAccountPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return persistenceRepository.existsByEmail(email.value());
    }

    @Override
    public Optional<UserAccount> findByPasswordResetTokenHash(HashedSecret tokenHash) {
        return persistenceRepository.findByPasswordResetTokenHash(tokenHash.hash())
                .map(UserAccountPersistenceAssembler::toDomainFromPersistence);
    }
}
