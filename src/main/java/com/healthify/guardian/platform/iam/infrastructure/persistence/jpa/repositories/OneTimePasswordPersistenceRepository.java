package com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.iam.domain.model.valueobjects.OtpPurpose;
import com.healthify.guardian.platform.iam.infrastructure.persistence.jpa.entities.OneTimePasswordPersistenceEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for one-time password persistence entities.
 */
@Repository
public interface OneTimePasswordPersistenceRepository extends JpaRepository<OneTimePasswordPersistenceEntity, UUID> {

    @Query("""
            select o from OneTimePasswordPersistenceEntity o
            where o.userAccountId = :userAccountId and o.purpose = :purpose
              and o.usedAt is null and o.expiresAt > :now and o.failedAttempts < :maxFailedAttempts
            order by o.createdAt desc""")
    List<OneTimePasswordPersistenceEntity> findUsable(@Param("userAccountId") UUID userAccountId,
                                                      @Param("purpose") OtpPurpose purpose,
                                                      @Param("now") Instant now,
                                                      @Param("maxFailedAttempts") int maxFailedAttempts,
                                                      Limit limit);

    int countByUserAccountIdAndPurposeAndCreatedAtAfter(UUID userAccountId, OtpPurpose purpose, Date since);

    @Modifying
    @Transactional
    @Query("delete from OneTimePasswordPersistenceEntity o where o.usedAt is null and o.expiresAt < :threshold")
    int deleteExpiredUnusedBefore(@Param("threshold") Instant threshold);
}
