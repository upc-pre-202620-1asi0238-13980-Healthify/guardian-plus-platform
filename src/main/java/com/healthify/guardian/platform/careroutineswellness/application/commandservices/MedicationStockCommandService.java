package com.healthify.guardian.platform.careroutineswellness.application.commandservices;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmMedicationAcquisitionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RegisterMedicationConsumptionCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RegisterMedicationStockCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.SuggestMedicationRestockCommand;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;

/**
 * Application service contract for commands over the {@code MedicationStock} aggregate.
 */
public interface MedicationStockCommandService {

    /**
     * Registers the stock of a new medication taken by a person under care.
     *
     * @param command the medication, its consumption, its package size and the doses at hand
     * @return the newly registered medication stock or an application error
     */
    Result<MedicationStock, ApplicationError> handle(RegisterMedicationStockCommand command);

    /**
     * Discounts the doses of a confirmed medication reminder from the matching stock: the one linked to the
     * reminder or, when none is linked, the person's stock of the medication with that name.
     *
     * @param command the person, the stock or medication name, and the doses taken
     * @return the updated medication stock, or a not-found error when the person has no matching stock
     */
    Result<MedicationStock, ApplicationError> handle(RegisterMedicationConsumptionCommand command);

    /**
     * Raises a restock suggestion if the stock's remaining supply is at or below the restock threshold.
     *
     * @param command the medication stock that must be evaluated
     * @return nothing on success, or an application error
     */
    Result<Void, ApplicationError> handle(SuggestMedicationRestockCommand command);

    /**
     * Confirms the acquisition of a new medication package, replenishing the stock.
     *
     * @param command the acquisition data
     * @return the updated medication stock or an application error
     */
    Result<MedicationStock, ApplicationError> handle(ConfirmMedicationAcquisitionCommand command);
}
