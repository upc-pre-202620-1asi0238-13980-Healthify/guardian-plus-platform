package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.entities.MedicationStockPersistenceEntity;

/**
 * Static assembler between the {@link MedicationStock} domain aggregate and its persistence entity.
 */
public final class MedicationStockPersistenceAssembler {

    private MedicationStockPersistenceAssembler() {
    }

    public static MedicationStock toDomainFromPersistence(MedicationStockPersistenceEntity entity) {
        if (entity == null) return null;
        var stock = new MedicationStock();
        stock.setId(new MedicationStockId(entity.getId()));
        stock.setPersonUnderCareId(entity.getPersonUnderCareId());
        stock.setRemainingDoses(entity.getRemainingDoses());
        stock.setDailyConsumption(entity.getDailyConsumption());
        stock.setLastAcquisitionDate(entity.getLastAcquisitionDate());
        return stock;
    }

    public static MedicationStockPersistenceEntity toPersistenceFromDomain(MedicationStock stock) {
        if (stock == null) return null;
        var entity = new MedicationStockPersistenceEntity();
        entity.setId(stock.getId().value());
        entity.setPersonUnderCareId(stock.getPersonUnderCareId());
        entity.setRemainingDoses(stock.getRemainingDoses());
        entity.setDailyConsumption(stock.getDailyConsumption());
        entity.setLastAcquisitionDate(stock.getLastAcquisitionDate());
        return entity;
    }
}
