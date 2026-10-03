package com.healthify.guardian.platform.careroutineswellness.domain.services;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Decides whether a medication stock's remaining balance warrants suggesting a restock.
 */
@Service
public class MedicationStockPolicy {

    private final BigDecimal restockThresholdDays;

    public MedicationStockPolicy(
            @Value("${care-routines-wellness.medication-stock.restock-threshold-days:3}") String restockThresholdDays) {
        this.restockThresholdDays = new BigDecimal(restockThresholdDays);
    }

    /**
     * Tells whether the given stock's projected remaining supply has dropped to, or below,
     * the configured restock threshold.
     *
     * @param stock the medication stock to evaluate
     * @return true if a restock suggestion should be raised
     */
    public boolean requiresRestockSuggestion(MedicationStock stock) {
        return stock.remainingDaysOfSupply().compareTo(restockThresholdDays) <= 0;
    }
}
