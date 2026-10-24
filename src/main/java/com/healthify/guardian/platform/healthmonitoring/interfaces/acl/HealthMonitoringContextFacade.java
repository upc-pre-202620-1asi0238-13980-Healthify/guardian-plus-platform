package com.healthify.guardian.platform.healthmonitoring.interfaces.acl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Public, read-only facade of the Health Monitoring bounded context for synchronous queries from
 * other contexts. It only exposes primitives, never the internal model.
 */
public interface HealthMonitoringContextFacade {

    /**
     * Whether the care recipient has a linked wearable device.
     */
    boolean hasLinkedWearableDevice(UUID careRecipientProfileId);

    /**
     * Latest emitted value of a vital sign type, identified by its code (e.g. {@code HR}); empty for unknown codes.
     */
    Optional<BigDecimal> fetchLatestVitalSignValue(UUID careRecipientProfileId, String vitalSignTypeCode);
}
