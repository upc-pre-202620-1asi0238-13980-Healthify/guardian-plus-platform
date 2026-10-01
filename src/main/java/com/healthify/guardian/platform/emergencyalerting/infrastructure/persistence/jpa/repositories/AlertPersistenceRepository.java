package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories;

import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSourceType;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data repository for alert persistence entities.
 *
 * <p>Extends {@link JpaSpecificationExecutor} so the alert history can combine its optional filters
 * (period and severity) without one query method per combination.</p>
 */
@Repository
public interface AlertPersistenceRepository
        extends JpaRepository<AlertPersistenceEntity, UUID>, JpaSpecificationExecutor<AlertPersistenceEntity> {

    @Query("""
            select a from AlertPersistenceEntity a
            where a.careRecipientProfileId = :careRecipientProfileId
              and a.source.sourceType = :sourceType
              and a.source.sourceReferenceId = :sourceReferenceId
              and a.status not in :excludedStatuses
            order by a.triggeredAt desc""")
    List<AlertPersistenceEntity> findBySourceAndStatusNotIn(
            @Param("careRecipientProfileId") CareRecipientProfileId careRecipientProfileId,
            @Param("sourceType") AlertSourceType sourceType,
            @Param("sourceReferenceId") UUID sourceReferenceId,
            @Param("excludedStatuses") Collection<AlertStatus> excludedStatuses);

    List<AlertPersistenceEntity> findByCareRecipientProfileIdAndStatusNotInOrderByTriggeredAtDesc(
            CareRecipientProfileId careRecipientProfileId, Collection<AlertStatus> excludedStatuses);

    @Query("""
            select distinct a from AlertPersistenceEntity a join a.deliveries d
            where d.recipientUserId = :recipientUserId
              and a.status in :statuses
            order by a.triggeredAt desc""")
    List<AlertPersistenceEntity> findByRecipientUserIdAndStatusIn(
            @Param("recipientUserId") UserId recipientUserId,
            @Param("statuses") Collection<AlertStatus> statuses);

    List<AlertPersistenceEntity> findByStatusAndTriggeredAtLessThanEqual(AlertStatus status, Instant threshold);

    List<AlertPersistenceEntity> findByStatusInAndLastDispatchedAtIsNotNull(Collection<AlertStatus> statuses);
}
