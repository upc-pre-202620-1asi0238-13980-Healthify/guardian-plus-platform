package com.healthify.guardian.platform.careroutineswellness.application.internal.queryservices;

import com.healthify.guardian.platform.careroutineswellness.application.queryservices.MedicationStockQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetMedicationStockStatusQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.repositories.MedicationStockRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that resolves medication stock read queries.
 */
@Service
public class MedicationStockQueryServiceImpl implements MedicationStockQueryService {

    private final MedicationStockRepository medicationStockRepository;

    public MedicationStockQueryServiceImpl(MedicationStockRepository medicationStockRepository) {
        this.medicationStockRepository = medicationStockRepository;
    }

    @Override
    public Optional<MedicationStock> handle(GetMedicationStockStatusQuery query) {
        return medicationStockRepository.findByPersonUnderCareId(query.personUnderCareId());
    }
}
