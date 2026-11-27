package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.MedicationStockRepository;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.MedicationStockPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.MedicationStockPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for the {@link MedicationStockRepository} domain port.
 */
@Repository
public class MedicationStockRepositoryImpl implements MedicationStockRepository {

    private final MedicationStockPersistenceRepository medicationStockPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MedicationStockRepositoryImpl(
            MedicationStockPersistenceRepository medicationStockPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.medicationStockPersistenceRepository = medicationStockPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<MedicationStock> findById(MedicationStockId id) {
        return medicationStockPersistenceRepository.findById(id.value())
                .map(MedicationStockPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<MedicationStock> findByPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        return medicationStockPersistenceRepository.findByPersonUnderCareIdOrderByMedicationName(personUnderCareId).stream()
                .map(MedicationStockPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public MedicationStock save(MedicationStock stock) {
        var entity = MedicationStockPersistenceAssembler.toPersistenceFromDomain(stock);
        var savedEntity = medicationStockPersistenceRepository.save(entity);
        stock.domainEvents().forEach(eventPublisher::publishEvent);
        stock.clearDomainEvents();
        return MedicationStockPersistenceAssembler.toDomainFromPersistence(savedEntity);
    }
}
