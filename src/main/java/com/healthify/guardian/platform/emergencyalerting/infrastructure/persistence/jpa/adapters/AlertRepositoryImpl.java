package com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertSource;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.AlertStatus;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.Severity;
import com.healthify.guardian.platform.emergencyalerting.domain.model.valueobjects.UserId;
import com.healthify.guardian.platform.emergencyalerting.domain.repositories.AlertRepository;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.assemblers.AlertPersistenceAssembler;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.entities.AlertPersistenceEntity;
import com.healthify.guardian.platform.emergencyalerting.infrastructure.persistence.jpa.repositories.AlertPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JPA adapter for the {@link AlertRepository} domain port.
 */
@Repository
public class AlertRepositoryImpl implements AlertRepository {

    private static final Set<AlertStatus> TERMINAL_STATUSES = EnumSet.of(AlertStatus.DISMISSED, AlertStatus.RESOLVED);
    private static final Set<AlertStatus> AWAITING_ACKNOWLEDGEMENT_STATUSES =
            EnumSet.of(AlertStatus.TRIGGERED, AlertStatus.ESCALATED);
    private static final Sort MOST_RECENT_FIRST = Sort.by(Sort.Direction.DESC, "triggeredAt");

    private final AlertPersistenceRepository alertPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AlertRepositoryImpl(
            AlertPersistenceRepository alertPersistenceRepository, ApplicationEventPublisher eventPublisher) {
        this.alertPersistenceRepository = alertPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Persists the alert and only then publishes its domain events. The events are detached from
     * the aggregate before publishing, because synchronous handlers may load and save this very
     * alert again (e.g. confirming it right after it is triggered).
     */
    @Override
    public Alert save(Alert alert) {
        var savedEntity = alertPersistenceRepository.save(AlertPersistenceAssembler.toPersistenceFromDomain(alert));
        var saved = AlertPersistenceAssembler.toDomainFromPersistence(savedEntity);
        var events = List.copyOf(alert.domainEvents());
        alert.clearDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        return saved;
    }

    @Override
    public Optional<Alert> findById(AlertId id) {
        return alertPersistenceRepository.findById(id.value())
                .map(AlertPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Alert> findActiveBySource(CareRecipientProfileId careRecipientProfileId, AlertSource source) {
        return alertPersistenceRepository.findBySourceAndStatusNotIn(
                        careRecipientProfileId, source.sourceType(), source.sourceReferenceId(), TERMINAL_STATUSES)
                .stream()
                .findFirst()
                .map(AlertPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Alert> findActiveByCareRecipientProfileId(CareRecipientProfileId careRecipientProfileId) {
        return alertPersistenceRepository
                .findByCareRecipientProfileIdAndStatusNotInOrderByTriggeredAtDesc(careRecipientProfileId, TERMINAL_STATUSES)
                .stream()
                .map(AlertPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Page<Alert> findHistory(
            CareRecipientProfileId careRecipientProfileId, DateRange period, Severity severity, Pageable pageable) {
        Specification<AlertPersistenceEntity> specification =
                (root, query, builder) -> builder.equal(root.get("careRecipientProfileId"), careRecipientProfileId);
        if (period != null && period.from() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.<Instant>get("triggeredAt"), period.from()));
        }
        if (period != null && period.to() != null) {
            specification = specification.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.<Instant>get("triggeredAt"), period.to()));
        }
        if (severity != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("severity"), severity));
        }
        var page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), MOST_RECENT_FIRST);
        return alertPersistenceRepository.findAll(specification, page)
                .map(AlertPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Alert> findPendingByRecipientUserId(UserId recipientUserId) {
        return alertPersistenceRepository
                .findByRecipientUserIdAndStatusIn(recipientUserId, AWAITING_ACKNOWLEDGEMENT_STATUSES)
                .stream()
                .map(AlertPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Alert> findPendingConfirmationTriggeredBefore(Instant threshold) {
        return alertPersistenceRepository
                .findByStatusAndTriggeredAtLessThanEqual(AlertStatus.PENDING_CONFIRMATION, threshold)
                .stream()
                .map(AlertPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<Alert> findAwaitingAcknowledgement() {
        return alertPersistenceRepository
                .findByStatusInAndLastDispatchedAtIsNotNull(AWAITING_ACKNOWLEDGEMENT_STATUSES)
                .stream()
                .map(AlertPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
