package com.healthify.guardian.platform.careroutineswellness.application.internal.queryservices;

import com.healthify.guardian.platform.careroutineswellness.application.queryservices.ReminderQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.Reminder;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetRemindersByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetReminderStatusByIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.ReminderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Application service that resolves reminder read queries.
 */
@Service
public class ReminderQueryServiceImpl implements ReminderQueryService {

    private final ReminderRepository reminderRepository;

    public ReminderQueryServiceImpl(ReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    @Override
    public Optional<Reminder> handle(GetReminderStatusByIdQuery query) {
        return reminderRepository.findById(query.reminderId());
    }

    @Override
    public List<Reminder> handle(GetRemindersByPersonUnderCareIdQuery query) {
        return reminderRepository.findByPersonUnderCareId(query.personUnderCareId());
    }
}
