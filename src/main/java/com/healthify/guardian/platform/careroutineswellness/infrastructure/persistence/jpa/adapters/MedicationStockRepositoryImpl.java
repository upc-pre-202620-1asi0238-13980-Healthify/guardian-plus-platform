package com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.adapters;

import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.MedicationStockRepository;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.assemblers.MedicationStockPersistenceAssembler;
import com.healthify.guardian.platform.careroutineswellness.infrastructure.persistence.jpa.repositories.MedicationStockPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

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
    public Optional<MedicationStock> findByPersonUnderCareId(PersonUnderCareId personUnderCareId) {
        return medicationStockPersistenceRepository.findByPersonUnderCareId(personUnderCareId)
                .map(MedicationStockPersistenceAssembler::toDomainFromPersistence);
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
