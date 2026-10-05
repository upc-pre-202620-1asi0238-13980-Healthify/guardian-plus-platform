package com.healthify.guardian.platform.healthmonitoring.domain.repositories;

import com.healthify.guardian.platform.healthmonitoring.domain.model.aggregates.VitalSignType;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeCode;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignTypeId;

import java.util.List;
import java.util.Optional;

/**
 * Vital sign type catalog repository port.
 */
public interface VitalSignTypeRepository {

    VitalSignType save(VitalSignType vitalSignType);

    Optional<VitalSignType> findById(VitalSignTypeId id);

    Optional<VitalSignType> findByCode(VitalSignTypeCode code);

    List<VitalSignType> findAll();
}
