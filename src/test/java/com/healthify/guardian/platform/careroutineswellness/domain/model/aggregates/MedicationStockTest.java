package com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates;

import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.RegisterMedicationStockCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationConsumedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationRestockSuggestedEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.events.MedicationStockRegisteredEvent;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RestockThreshold;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicationStockTest {

    private static final UUID PERSON = UUID.randomUUID();

    private static MedicationStock losartan(int initialDoses) {
        return new MedicationStock(new RegisterMedicationStockCommand(
                PERSON, "Losartán", "50 mg", BigDecimal.valueOf(2), 30, initialDoses));
    }

    @Test
    void registeringDescribesTheMedication() {
        var stock = losartan(6);

        assertThat(stock.getMedication().name()).isEqualTo("Losartán");
        assertThat(stock.getMedication().dosage()).isEqualTo("50 mg");
        assertThat(stock.remainingDaysOfSupply()).isEqualByComparingTo("3");
        assertThat(stock.domainEvents()).singleElement().isInstanceOf(MedicationStockRegisteredEvent.class);
    }

    @Test
    void acquisitionWithoutAnAmountAddsAWholePackage() {
        var stock = losartan(6);

        stock.confirmAcquisition(null);

        assertThat(stock.getRemainingDoses()).isEqualTo(36);
    }

    @Test
    void projectsTheDepletionDate() {
        var stock = losartan(7);

        assertThat(stock.estimatedDepletionDate(LocalDate.of(2026, 10, 8))).isEqualTo(LocalDate.of(2026, 10, 11));
    }

    @Test
    void recognizesItsMedicationIgnoringCase() {
        var stock = losartan(6);

        assertThat(stock.tracks(" losartán ")).isTrue();
        assertThat(stock.tracks("Metformina")).isFalse();
    }

    @Test
    void restockSuggestionNamesTheMedication() {
        var stock = losartan(6);
        stock.clearDomainEvents();

        stock.suggestRestock();

        assertThat(stock.domainEvents()).singleElement()
                .isInstanceOfSatisfying(MedicationRestockSuggestedEvent.class,
                        event -> assertThat(event.medicationName()).isEqualTo("Losartán"));
    }

    @Test
    void rejectsAMissingNameOrPackageSize() {
        assertThatThrownBy(() -> new MedicationStock(new RegisterMedicationStockCommand(
                PERSON, " ", null, BigDecimal.ONE, 30, 0)))
                .hasMessage("medication-stock.medication-name.blank");
        assertThatThrownBy(() -> new MedicationStock(new RegisterMedicationStockCommand(
                PERSON, "Metformina", null, BigDecimal.ONE, 0, 0)))
                .hasMessage("medication-stock.package-size.non-positive");
    }

    @Test
    void runsLowAtOrBelowTheRestockThreshold() {
        var threeDays = new RestockThreshold(BigDecimal.valueOf(3));

        assertThat(losartan(8).requiresRestock(threeDays)).isFalse();
        assertThat(losartan(6).requiresRestock(threeDays)).isTrue();
    }

    @Test
    void consumingADoseRaisesTheEventTheRestockPolicyReactsTo() {
        var stock = losartan(8);
        stock.clearDomainEvents();

        stock.registerConsumption(1);

        assertThat(stock.getRemainingDoses()).isEqualTo(7);
        assertThat(stock.domainEvents()).singleElement()
                .isInstanceOfSatisfying(MedicationConsumedEvent.class,
                        event -> assertThat(event.remainingDoses()).isEqualTo(7));
    }
}
