package com.healthify.guardian.platform.careroutineswellness.application.internal.queryservices;

import com.healthify.guardian.platform.careroutineswellness.application.queryservices.SleepCycleRecordQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetSleepCycleRecordsByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.SleepCycleRecordRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Application service that resolves sleep cycle record read queries.
 */
@Service
public class SleepCycleRecordQueryServiceImpl implements SleepCycleRecordQueryService {

    private final SleepCycleRecordRepository sleepCycleRecordRepository;

    public SleepCycleRecordQueryServiceImpl(SleepCycleRecordRepository sleepCycleRecordRepository) {
        this.sleepCycleRecordRepository = sleepCycleRecordRepository;
    }

    @Override
    public List<SleepCycleRecord> handle(GetSleepCycleRecordsByPersonUnderCareIdQuery query) {
        return sleepCycleRecordRepository.findByPersonUnderCareId(query.personUnderCareId(), query.from(), query.to());
    }
}
