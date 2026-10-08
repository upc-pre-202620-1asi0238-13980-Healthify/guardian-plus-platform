package com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.SleepCycleRecordCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.SleepCycleRecord;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RecordSleepCycleCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.SleepCycleRecordRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

/**
 * Application service that executes sleep cycle record commands.
 */
@Service
public class SleepCycleRecordCommandServiceImpl implements SleepCycleRecordCommandService {

    private final SleepCycleRecordRepository sleepCycleRecordRepository;

    public SleepCycleRecordCommandServiceImpl(SleepCycleRecordRepository sleepCycleRecordRepository) {
        this.sleepCycleRecordRepository = sleepCycleRecordRepository;
    }

    @Override
    public Result<SleepCycleRecord, ApplicationError> handle(RecordSleepCycleCommand command) {
        try {
            var record = new SleepCycleRecord(command);
            return Result.success(sleepCycleRecordRepository.save(record));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "record-sleep-cycle", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }
}
