package com.healthify.guardian.platform.careroutineswellness.interfaces.rest;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.MedicationStockCommandService;
import com.healthify.guardian.platform.careroutineswellness.application.queryservices.MedicationStockQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetMedicationStockStatusQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ConfirmMedicationAcquisitionResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.MedicationStockResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ConfirmMedicationAcquisitionCommandFromResourceAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.MedicationStockResourceFromEntityAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for medication stock endpoints.
 *
 * <p>Routes are keyed by {@code personUnderCareId} rather than the stock's own identifier,
 * consistent with the domain model: exactly one medication stock exists per person under care,
 * created on demand by the very first acquisition confirmation.</p>
 */
@RestController
@RequestMapping(value = "/api/v1/medication-stocks", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Medication Stock", description = "Medication stock balance and restock endpoints")
public class MedicationStockController {

    private final MedicationStockCommandService medicationStockCommandService;
    private final MedicationStockQueryService medicationStockQueryService;

    public MedicationStockController(
            MedicationStockCommandService medicationStockCommandService,
            MedicationStockQueryService medicationStockQueryService) {
        this.medicationStockCommandService = medicationStockCommandService;
        this.medicationStockQueryService = medicationStockQueryService;
    }

    @PutMapping("/citizen/{personUnderCareId}/acquisition")
    @Operation(
            summary = "Confirm a medication acquisition",
            description = "Confirms the acquisition of a new medication package, creating the stock on its very " +
                    "first acquisition or replenishing it otherwise."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Medication acquisition confirmed successfully",
                    content = @Content(schema = @Schema(implementation = MedicationStockResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> confirmMedicationAcquisition(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId,
            @Valid @RequestBody ConfirmMedicationAcquisitionResource resource
    ) {
        var command = ConfirmMedicationAcquisitionCommandFromResourceAssembler
                .toCommandFromResource(personUnderCareId, resource);
        var result = medicationStockCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, MedicationStockResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @GetMapping("/citizen/{personUnderCareId}")
    @Operation(
            summary = "Get medication stock status",
            description = "Retrieves the current medication stock balance and projected days of supply for a person under care."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Medication stock found",
                    content = @Content(schema = @Schema(implementation = MedicationStockResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Medication stock not found")
    })
    public ResponseEntity<?> getMedicationStockStatus(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId
    ) {
        var query = new GetMedicationStockStatusQuery(new PersonUnderCareId(personUnderCareId));
        var stock = medicationStockQueryService.handle(query);
        if (stock.isEmpty()) {
            var error = ApplicationError.notFound("MedicationStock", personUnderCareId.toString());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }
        return ResponseEntity.ok(MedicationStockResourceFromEntityAssembler.toResourceFromEntity(stock.get()));
    }
}
