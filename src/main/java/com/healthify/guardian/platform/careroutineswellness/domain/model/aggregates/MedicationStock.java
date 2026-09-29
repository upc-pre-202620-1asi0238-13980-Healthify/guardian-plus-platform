package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationRestockSuggestedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationStockUpdatedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Aggregate root representing the remaining-dose balance of a medication treatment for a
 * person under care, together with the acquisition history needed to keep it topped up.
 */
@Getter
public class MedicationStock extends AbstractDomainAggregateRoot<MedicationStock> {

    private static final String NEGATIVE_DOSES_MESSAGE_KEY = "medication-stock.doses.negative";
    private static final String NON_POSITIVE_CONSUMPTION_MESSAGE_KEY = "medication-stock.daily-consumption.non-positive";

    private MedicationStockId id;
    private PersonUnderCareId personUnderCareId;
    private Integer remainingDoses;
    private BigDecimal dailyConsumption;
    private Instant lastAcquisitionDate;

    /** Reconstitution constructor, used by the persistence assembler. */
    public MedicationStock() {
    }

    /**
     * Creates a new medication stock, seeded by the first confirmed acquisition.
     *
     * @param personUnderCareId the person this stock belongs to
     * @param initialDoses      doses available right after the first acquisition
     * @param dailyConsumption  expected doses consumed per day, used to project remaining supply
     */
    public MedicationStock(PersonUnderCareId personUnderCareId, Integer initialDoses, BigDecimal dailyConsumption) {
        if (initialDoses == null || initialDoses < 0) {
            throw new IllegalArgumentException(NEGATIVE_DOSES_MESSAGE_KEY);
        }
        if (dailyConsumption == null || dailyConsumption.signum() <= 0) {
            throw new IllegalArgumentException(NON_POSITIVE_CONSUMPTION_MESSAGE_KEY);
        }
        this.id = MedicationStockId.generate();
        this.personUnderCareId = personUnderCareId;
        this.remainingDoses = initialDoses;
        this.dailyConsumption = dailyConsumption;
        this.lastAcquisitionDate = Instant.now();
    }

    /**
     * Registers the consumption of doses, typically after a medication reminder is confirmed.
     *
     * @param dosesConsumed number of doses just taken; the balance never drops below zero
     */
    public void registerConsumption(Integer dosesConsumed) {
        if (dosesConsumed == null || dosesConsumed <= 0) {
            return;
        }
        this.remainingDoses = Math.max(0, this.remainingDoses - dosesConsumed);
    }

    /**
     * Confirms the acquisition of a new medication package, replenishing the balance.
     *
     * @param dosesAdded number of doses added by the new package
     */
    public void confirmAcquisition(Integer dosesAdded) {
        var now = Instant.now();
        this.remainingDoses = this.remainingDoses + dosesAdded;
        this.lastAcquisitionDate = now;
        registerDomainEvent(MedicationStockUpdatedEvent.from(this, now));
    }

    /** Raises the integration-worthy suggestion that this stock should be replenished soon. */
    public void suggestRestock() {
        registerDomainEvent(MedicationRestockSuggestedEvent.from(this, Instant.now()));
    }

    /**
     * Projects how many days the remaining doses will last at the current daily consumption rate.
     *
     * @return remaining days of supply, rounded to two decimal places
     */
    public BigDecimal remainingDaysOfSupply() {
        return BigDecimal.valueOf(remainingDoses)
                .divide(dailyConsumption, 2, RoundingMode.HALF_UP);
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(MedicationStockId id) {
        this.id = id;
    }

    public void setPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        this.personUnderCareId = personUnderCareId;
    }

    public void setRemainingDoses(Integer remainingDoses) {
        this.remainingDoses = remainingDoses;
    }

    public void setDailyConsumption(BigDecimal dailyConsumption) {
        this.dailyConsumption = dailyConsumption;
    }

    public void setLastAcquisitionDate(Instant lastAcquisitionDate) {
        this.lastAcquisitionDate = lastAcquisitionDate;
    }
}
