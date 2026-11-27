package com.healthify.guardian.platform.mobilitygeofencing.domain.model.repositories;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.entities.CallLogEntry;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;

import java.util.List;

public interface CallLogRepository {
    CallLogEntry save(CallLogEntry callLogEntry);
    List<CallLogEntry> findByFragileCitizenIdOrderByOccurredAtDesc(FragileCitizenId fragileCitizenId);
}
