package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload registering a vital sign type in the catalog.
 */
public record RegisterVitalSignTypeResource(
        @NotBlank(message = "{vital-sign-type.code.invalid}")
        @Schema(example = "GLUCOSE")
        String code,

        @NotBlank(message = "{vital-sign-type.name.invalid}")
        @Schema(example = "Blood glucose")
        String name,

        @NotBlank(message = "{vital-sign-type.unit.invalid}")
        @Schema(example = "mg/dL")
        String unit
) {
}
