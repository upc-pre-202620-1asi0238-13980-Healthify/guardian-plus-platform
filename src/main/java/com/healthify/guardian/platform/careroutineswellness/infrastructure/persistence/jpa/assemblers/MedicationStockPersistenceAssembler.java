package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.Medication;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.MedicationStockPersistenceEntity;

/**
 * Static assembler between the {@link MedicationStock} domain aggregate and its persistence entity.
 *
 * <p>Rows written before stocks were tracked per medication have no name; they are reconstituted under a
 * placeholder name so they can still be listed and replenished.</p>
 */
public final class MedicationStockPersistenceAssembler {

    private static final String UNNAMED_MEDICATION = "Unnamed medication";

    private MedicationStockPersistenceAssembler() {
    }

    public static MedicationStock toDomainFromPersistence(MedicationStockPersistenceEntity entity) {
        if (entity == null) return null;
        var stock = new MedicationStock();
        stock.setId(new MedicationStockId(entity.getId()));
        stock.setPersonUnderCareId(entity.getPersonUnderCareId());
        stock.setMedication(new Medication(
                entity.getMedicationName() != null ? entity.getMedicationName() : UNNAMED_MEDICATION, entity.getDosage()));
        stock.setRemainingDoses(entity.getRemainingDoses());
        stock.setDailyConsumption(entity.getDailyConsumption());
        stock.setPackageSize(entity.getPackageSize());
        stock.setLastAcquisitionDate(entity.getLastAcquisitionDate());
        return stock;
    }

    public static MedicationStockPersistenceEntity toPersistenceFromDomain(MedicationStock stock) {
        if (stock == null) return null;
        var entity = new MedicationStockPersistenceEntity();
        entity.setId(stock.getId().value());
        entity.setPersonUnderCareId(stock.getPersonUnderCareId());
        entity.setMedicationName(stock.getMedication().name());
        entity.setDosage(stock.getMedication().dosage());
        entity.setRemainingDoses(stock.getRemainingDoses());
        entity.setDailyConsumption(stock.getDailyConsumption());
        entity.setPackageSize(stock.getPackageSize());
        entity.setLastAcquisitionDate(stock.getLastAcquisitionDate());
        return entity;
    }
}
