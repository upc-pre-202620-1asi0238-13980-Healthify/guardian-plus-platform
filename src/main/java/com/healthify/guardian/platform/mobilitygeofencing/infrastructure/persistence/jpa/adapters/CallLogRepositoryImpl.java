package com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities.CallLogEntry;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.repositories.CallLogRepository;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallStatus;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.CallType;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.entities.CallLogJpaEntity;
import com.healthify.guardian.platform.mobilitygeofencing.infrastructure.persistence.jpa.repositories.SpringDataCallLogRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CallLogRepositoryImpl implements CallLogRepository {

    private final SpringDataCallLogRepository jpaRepository;

    public CallLogRepositoryImpl(SpringDataCallLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CallLogEntry save(CallLogEntry callLogEntry) {
        CallLogJpaEntity entity = toEntity(callLogEntry);
        CallLogJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public List<CallLogEntry> findByFragileCitizenIdOrderByOccurredAtDesc(FragileCitizenId fragileCitizenId) {
        return jpaRepository.findAllByFragileCitizenIdOrderByOccurredAtDesc(fragileCitizenId.value())
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private CallLogJpaEntity toEntity(CallLogEntry domain) {
        return new CallLogJpaEntity(
                domain.getId(),
                domain.getFragileCitizenId().value(),
                domain.getCallType().name(),
                domain.getStatus().name(),
                domain.isDowngraded(),
                domain.getReason(),
                domain.getDurationSeconds(),
                domain.getOccurredAt()
        );
    }

    private CallLogEntry toDomain(CallLogJpaEntity entity) {
        return new CallLogEntry(
                entity.getId(),
                new FragileCitizenId(entity.getFragileCitizenId()),
                CallType.valueOf(entity.getCallType()),
                CallStatus.valueOf(entity.getStatus()),
                entity.isDowngraded(),
                entity.getReason(),
                entity.getDurationSeconds(),
                entity.getOccurredAt()
        );
    }
}
