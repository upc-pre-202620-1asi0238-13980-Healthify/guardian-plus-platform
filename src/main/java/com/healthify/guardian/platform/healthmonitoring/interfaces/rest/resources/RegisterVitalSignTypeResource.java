package com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request payload registering a vital sign type in the catalog, with its normal range and the
 * physical limits of any reading of that type.
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
        String unit,

        @NotNull(message = "{vital-sign-range.invalid}")
        @Schema(description = "Lower bound of the normal range, used as the default threshold", example = "70")
        BigDecimal normalMinimum,

        @NotNull(message = "{vital-sign-range.invalid}")
        @Schema(description = "Upper bound of the normal range, used as the default threshold", example = "140")
        BigDecimal normalMaximum,

        @NotNull(message = "{vital-sign-range.invalid}")
        @Schema(description = "Lowest physically possible reading; anything below is rejected", example = "10")
        BigDecimal physicalMinimum,

        @NotNull(message = "{vital-sign-range.invalid}")
        @Schema(description = "Highest physically possible reading; anything above is rejected", example = "600")
        BigDecimal physicalMaximum
) {
}
