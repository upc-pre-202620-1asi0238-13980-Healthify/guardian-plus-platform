package com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.MedicationStockCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmMedicationAcquisitionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.SuggestMedicationRestockCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.MedicationStockRepository;
import com.healthify.guardian.platform.careroutineswellness.domain.services.MedicationStockPolicy;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Application service that executes medication stock commands.
 */
@Service
public class MedicationStockCommandServiceImpl implements MedicationStockCommandService {

    private final MedicationStockRepository medicationStockRepository;
    private final MedicationStockPolicy medicationStockPolicy;
    private final BigDecimal defaultDailyConsumption;

    public MedicationStockCommandServiceImpl(
            MedicationStockRepository medicationStockRepository,
            MedicationStockPolicy medicationStockPolicy,
            @Value("${care-routines-wellness.medication-stock.default-daily-consumption:1}")
            String defaultDailyConsumption) {
        this.medicationStockRepository = medicationStockRepository;
        this.medicationStockPolicy = medicationStockPolicy;
        this.defaultDailyConsumption = new BigDecimal(defaultDailyConsumption);
    }

    @Override
    public Result<Void, ApplicationError> handle(SuggestMedicationRestockCommand command) {
        var stock = medicationStockRepository.findByPersonUnderCareId(
                new PersonUnderCareId(command.personUnderCareId()));
        if (stock.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MedicationStock", command.personUnderCareId().toString()));
        }

        if (medicationStockPolicy.requiresRestockSuggestion(stock.get())) {
            stock.get().suggestRestock();
            medicationStockRepository.save(stock.get());
        }
        return Result.success(null);
    }

    @Override
    public Result<MedicationStock, ApplicationError> handle(ConfirmMedicationAcquisitionCommand command) {
        try {
            var personUnderCareId = new PersonUnderCareId(command.personUnderCareId());
            var existing = medicationStockRepository.findByPersonUnderCareId(personUnderCareId);

            if (existing.isPresent()) {
                existing.get().confirmAcquisition(command.dosesAdded());
                return Result.success(medicationStockRepository.save(existing.get()));
            }

            var created = new MedicationStock(personUnderCareId, command.dosesAdded(), defaultDailyConsumption);
            return Result.success(medicationStockRepository.save(created));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "confirm-medication-acquisition", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }
}
