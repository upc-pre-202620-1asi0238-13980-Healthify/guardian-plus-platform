package com.healthify.guardian.platform.careroutineswellness.application.commandservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmMedicationAcquisitionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.SuggestMedicationRestockCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code MedicationStock} aggregate.
 */
public interface MedicationStockCommandService {

    /**
     * Evaluates {@code MedicationStockPolicy} against the current balance and, if it applies,
     * raises a restock suggestion.
     *
     * @param command the person under care whose stock must be evaluated
     * @return nothing on success, or an application error
     */
    Result<Void, ApplicationError> handle(SuggestMedicationRestockCommand command);

    /**
     * Confirms the acquisition of a new medication package, creating the stock on its very
     * first acquisition or replenishing it otherwise.
     *
     * @param command the acquisition data
     * @return the updated medication stock or an application error
     */
    Result<MedicationStock, ApplicationError> handle(ConfirmMedicationAcquisitionCommand command);
}
