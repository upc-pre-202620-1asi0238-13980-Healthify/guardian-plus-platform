package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import java.util.UUID;

/**
 * Response payload of a vital sign type.
 */
public record VitalSignTypeResource(
        UUID id,
        String code,
        String name,
        String unit
) {
}
