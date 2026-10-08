package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RegisterMedicationStockCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationConsumedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationRestockSuggestedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationStockRegisteredEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationStockUpdatedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.Medication;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RestockThreshold;
import com.healthify.guardian.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Aggregate root representing the remaining-dose balance of one medication treatment of a person
 * under care, together with the acquisition history needed to keep it topped up.
 *
 * <p>A person under care has one stock per medication they take.</p>
 */
@Getter
public class MedicationStock extends AbstractDomainAggregateRoot<MedicationStock> {

    private static final String NEGATIVE_DOSES_MESSAGE_KEY = "medication-stock.doses.negative";
    private static final String NON_POSITIVE_CONSUMPTION_MESSAGE_KEY = "medication-stock.daily-consumption.non-positive";
    private static final String NON_POSITIVE_PACKAGE_SIZE_MESSAGE_KEY = "medication-stock.package-size.non-positive";
    private static final String NON_POSITIVE_DOSES_ADDED_MESSAGE_KEY = "medication-stock.doses-added.invalid";

    private MedicationStockId id;
    private PersonUnderCareId personUnderCareId;
    private Medication medication;
    private Integer remainingDoses;
    private BigDecimal dailyConsumption;
    private Integer packageSize;
    private Instant lastAcquisitionDate;

    /** Reconstitution constructor, used by the persistence assembler. */
    public MedicationStock() {
    }

    /**
     * Registers the stock of a new medication taken by a person under care.
     *
     * @param command the medication, its daily consumption, its package size and the doses already at hand
     */
    public MedicationStock(RegisterMedicationStockCommand command) {
        var initialDoses = command.initialDoses() == null ? 0 : command.initialDoses();
        if (initialDoses < 0) {
            throw new IllegalArgumentException(NEGATIVE_DOSES_MESSAGE_KEY);
        }
        if (command.dailyConsumption() == null || command.dailyConsumption().signum() <= 0) {
            throw new IllegalArgumentException(NON_POSITIVE_CONSUMPTION_MESSAGE_KEY);
        }
        if (command.packageSize() == null || command.packageSize() <= 0) {
            throw new IllegalArgumentException(NON_POSITIVE_PACKAGE_SIZE_MESSAGE_KEY);
        }
        this.id = MedicationStockId.generate();
        this.personUnderCareId = new PersonUnderCareId(command.personUnderCareId());
        this.medication = new Medication(command.medicationName(), command.dosage());
        this.remainingDoses = initialDoses;
        this.dailyConsumption = command.dailyConsumption();
        this.packageSize = command.packageSize();
        this.lastAcquisitionDate = Instant.now();
        registerDomainEvent(MedicationStockRegisteredEvent.from(this));
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
        registerDomainEvent(MedicationConsumedEvent.from(this, dosesConsumed, Instant.now()));
    }

    /**
     * Tells whether the projected remaining supply has dropped to, or below, the restock threshold.
     *
     * @param threshold days of supply at or below which the stock should be replenished
     * @return true if a restock should be suggested
     */
    public boolean requiresRestock(RestockThreshold threshold) {
        return remainingDaysOfSupply().compareTo(threshold.days()) <= 0;
    }

    /**
     * Confirms the acquisition of a new medication package, replenishing the balance.
     *
     * @param dosesAdded number of doses added; a whole package ({@link #packageSize}) when null
     */
    public void confirmAcquisition(Integer dosesAdded) {
        var added = dosesAdded == null ? packageSize : dosesAdded;
        if (added == null || added <= 0) {
            throw new IllegalArgumentException(NON_POSITIVE_DOSES_ADDED_MESSAGE_KEY);
        }
        var now = Instant.now();
        this.remainingDoses = this.remainingDoses + added;
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

    /**
     * Projects the day the remaining doses run out at the current daily consumption rate.
     *
     * @param today the current date in the person under care's zone
     * @return the estimated depletion date
     */
    public LocalDate estimatedDepletionDate(LocalDate today) {
        return today.plusDays(remainingDaysOfSupply().setScale(0, RoundingMode.FLOOR).longValue());
    }

    /**
     * Tells whether this stock tracks the medication with the given name.
     *
     * @param medicationName the name to compare against, ignoring case
     * @return true if it is the same medication
     */
    public boolean tracks(String medicationName) {
        return medication != null && medication.isNamed(medicationName);
    }

    /** Restores an identity and state from persistence. Used by the persistence assembler. */
    public void setId(MedicationStockId id) {
        this.id = id;
    }

    public void setPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        this.personUnderCareId = personUnderCareId;
    }

    public void setMedication(Medication medication) {
        this.medication = medication;
    }

    public void setRemainingDoses(Integer remainingDoses) {
        this.remainingDoses = remainingDoses;
    }

    public void setDailyConsumption(BigDecimal dailyConsumption) {
        this.dailyConsumption = dailyConsumption;
    }

    public void setPackageSize(Integer packageSize) {
        this.packageSize = packageSize;
    }

    public void setLastAcquisitionDate(Instant lastAcquisitionDate) {
        this.lastAcquisitionDate = lastAcquisitionDate;
    }
}
