package com.healthify.guardian.platform.careroutineswellness.application.internal.commandservices;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.MedicationStockCommandService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmMedicationAcquisitionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RegisterMedicationConsumptionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RegisterMedicationStockCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.SuggestMedicationRestockCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RestockThreshold;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.MedicationStockRepository;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that executes medication stock commands.
 */
@Service
public class MedicationStockCommandServiceImpl implements MedicationStockCommandService {

    private static final String ALREADY_REGISTERED_KEY = "medication-stock.medication.already-registered";

    private final MedicationStockRepository medicationStockRepository;
    private final RestockThreshold restockThreshold;

    public MedicationStockCommandServiceImpl(
            MedicationStockRepository medicationStockRepository,
            RestockThreshold careRoutinesWellnessRestockThreshold) {
        this.medicationStockRepository = medicationStockRepository;
        this.restockThreshold = careRoutinesWellnessRestockThreshold;
    }

    @Override
    public Result<MedicationStock, ApplicationError> handle(RegisterMedicationStockCommand command) {
        try {
            var alreadyTracked = medicationStockRepository
                    .findByPersonUnderCareId(new PersonUnderCareId(command.personUnderCareId())).stream()
                    .anyMatch(stock -> stock.tracks(command.medicationName()));
            if (alreadyTracked) {
                return Result.failure(ApplicationError.conflict("MedicationStock",
                        MessageResolver.resolveOrDefault(ALREADY_REGISTERED_KEY, ALREADY_REGISTERED_KEY)));
            }
            return Result.success(medicationStockRepository.save(new MedicationStock(command)));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "register-medication-stock", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }

    @Override
    public Result<MedicationStock, ApplicationError> handle(RegisterMedicationConsumptionCommand command) {
        var stock = findConsumedStock(command);
        if (stock.isEmpty()) {
            var reference = command.medicationStockId() != null ? command.medicationStockId().toString() : command.medicationName();
            return Result.failure(ApplicationError.notFound("MedicationStock", reference));
        }
        stock.get().registerConsumption(command.doses());
        return Result.success(medicationStockRepository.save(stock.get()));
    }

    @Override
    public Result<Void, ApplicationError> handle(SuggestMedicationRestockCommand command) {
        var stock = medicationStockRepository.findById(new MedicationStockId(command.medicationStockId()));
        if (stock.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MedicationStock", command.medicationStockId().toString()));
        }

        if (stock.get().requiresRestock(restockThreshold)) {
            stock.get().suggestRestock();
            medicationStockRepository.save(stock.get());
        }
        return Result.success(null);
    }

    @Override
    public Result<MedicationStock, ApplicationError> handle(ConfirmMedicationAcquisitionCommand command) {
        var stock = medicationStockRepository.findById(new MedicationStockId(command.medicationStockId()));
        if (stock.isEmpty()) {
            return Result.failure(ApplicationError.notFound("MedicationStock", command.medicationStockId().toString()));
        }
        try {
            stock.get().confirmAcquisition(command.dosesAdded());
            return Result.success(medicationStockRepository.save(stock.get()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError(
                    "confirm-medication-acquisition", MessageResolver.resolveOrDefault(e.getMessage(), e.getMessage())));
        }
    }

    private Optional<MedicationStock> findConsumedStock(RegisterMedicationConsumptionCommand command) {
        if (command.medicationStockId() != null) {
            return medicationStockRepository.findById(new MedicationStockId(command.medicationStockId()));
        }
        return medicationStockRepository.findByPersonUnderCareId(new PersonUnderCareId(command.personUnderCareId())).stream()
                .filter(stock -> stock.tracks(command.medicationName()))
                .findFirst();
    }
}
