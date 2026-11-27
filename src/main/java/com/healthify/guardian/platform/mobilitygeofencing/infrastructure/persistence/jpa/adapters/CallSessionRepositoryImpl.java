package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.CallSession;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.repositories.CallSessionRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallStatus;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallType;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.CallSessionJpaEntity;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories.SpringDataCallSessionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CallSessionRepositoryImpl implements CallSessionRepository {

    private final SpringDataCallSessionRepository jpaRepository;

    public CallSessionRepositoryImpl(SpringDataCallSessionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CallSession save(CallSession callSession) {
        CallSessionJpaEntity entity = toEntity(callSession);
        CallSessionJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<CallSession> findById(UUID callId) {
        return jpaRepository.findById(callId)
                .map(this::toDomain);
    }

    @Override
    public List<CallSession> findByFragileCitizenId(FragileCitizenId fragileCitizenId) {
        return jpaRepository.findAllByFragileCitizenIdOrderByStartedAtDesc(fragileCitizenId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CallSessionJpaEntity toEntity(CallSession domain) {
        return new CallSessionJpaEntity(
                domain.getId(),
                domain.getFragileCitizenId().value(),
                domain.getRequestedType().name(),
                domain.getActiveType().name(),
                domain.getStatus().name(),
                domain.isDowngraded(),
                domain.getReasonMessage(),
                domain.getStartedAt(),
                domain.getEndedAt(),
                domain.getDurationSeconds()
        );
    }

    private CallSession toDomain(CallSessionJpaEntity entity) {
        return new CallSession(
                entity.getId(),
                new FragileCitizenId(entity.getFragileCitizenId()),
                CallType.valueOf(entity.getRequestedType()),
                CallType.valueOf(entity.getActiveType()),
                CallStatus.valueOf(entity.getStatus()),
                entity.isDowngraded(),
                entity.getReasonMessage(),
                entity.getStartedAt(),
                entity.getEndedAt(),
                entity.getDurationSeconds()
        );
    }
}
