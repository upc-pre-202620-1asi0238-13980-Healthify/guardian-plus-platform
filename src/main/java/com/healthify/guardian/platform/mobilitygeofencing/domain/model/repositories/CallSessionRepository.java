package com.healthify.guardian.platform.mobilitygeofencing.domain.model.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.CallSession;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallSessionRepository {
    CallSession save(CallSession callSession);
    Optional<CallSession> findById(UUID callId);
    List<CallSession> findByFragileCitizenId(FragileCitizenId fragileCitizenId);
}
