package com.healthify.guardian.platform.careroutineswellness.interfaces.rest;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.MedicationStockCommandService;
import com.healthify.guardian.platform.careroutineswellness.application.queryservices.MedicationStockQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.aggregates.MedicationStock;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetMedicationStockByIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetMedicationStocksByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.MedicationStockId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.RestockThreshold;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ConfirmMedicationAcquisitionResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.MedicationStockResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.RegisterMedicationStockResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ConfirmMedicationAcquisitionCommandFromResourceAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.MedicationStockResourceFromEntityAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.RegisterMedicationStockCommandFromResourceAssembler;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for medication stock endpoints. A person under care has one stock per medication.
 */
@RestController
@RequestMapping(value = "/api/v1/medication-stocks", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Medication Stock", description = "Medication stock balance and restock endpoints")
public class MedicationStockController {

    private final MedicationStockCommandService medicationStockCommandService;
    private final MedicationStockQueryService medicationStockQueryService;
    private final RestockThreshold restockThreshold;
    private final ZoneId zone;

    public MedicationStockController(
            MedicationStockCommandService medicationStockCommandService,
            MedicationStockQueryService medicationStockQueryService,
            RestockThreshold careRoutinesWellnessRestockThreshold,
            ZoneId careRoutinesWellnessZoneId) {
        this.medicationStockCommandService = medicationStockCommandService;
        this.medicationStockQueryService = medicationStockQueryService;
        this.restockThreshold = careRoutinesWellnessRestockThreshold;
        this.zone = careRoutinesWellnessZoneId;
    }

    @PostMapping
    @Operation(
            summary = "Register a medication stock",
            description = "Registers the stock of a new medication taken by a person under care: its name, dose, " +
                    "daily consumption, package size and the doses already at hand."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Medication stock registered successfully",
                    content = @Content(schema = @Schema(implementation = MedicationStockResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "The person already has a stock for this medication")
    })
    public ResponseEntity<?> registerMedicationStock(@Valid @RequestBody RegisterMedicationStockResource resource) {
        var command = RegisterMedicationStockCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = medicationStockCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result, this::toResource, HttpStatus.CREATED);
    }

    @PutMapping("/{medicationStockId}/acquisition")
    @Operation(
            summary = "Confirm a medication acquisition",
            description = "Confirms the acquisition of a new medication package (\"Añadir envase\"), replenishing the " +
                    "stock. Without a body, or without dosesAdded, a whole package is added."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Medication acquisition confirmed successfully",
                    content = @Content(schema = @Schema(implementation = MedicationStockResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Medication stock not found")
    })
    public ResponseEntity<?> confirmMedicationAcquisition(
            @PathVariable
            @Parameter(description = "Medication stock unique identifier", required = true)
            UUID medicationStockId,
            @Valid @RequestBody(required = false) ConfirmMedicationAcquisitionResource resource
    ) {
        var command = ConfirmMedicationAcquisitionCommandFromResourceAssembler
                .toCommandFromResource(medicationStockId, resource);
        var result = medicationStockCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(result, this::toResource, HttpStatus.OK);
    }

    @GetMapping("/{medicationStockId}")
    @Operation(
            summary = "Get a medication stock",
            description = "Retrieves the balance, projected days of supply and restock verdict of one medication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Medication stock found",
                    content = @Content(schema = @Schema(implementation = MedicationStockResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Medication stock not found")
    })
    public ResponseEntity<?> getMedicationStockById(
            @PathVariable
            @Parameter(description = "Medication stock unique identifier", required = true)
            UUID medicationStockId
    ) {
        var stock = medicationStockQueryService.handle(new GetMedicationStockByIdQuery(new MedicationStockId(medicationStockId)));
        if (stock.isEmpty()) {
            var error = ApplicationError.notFound("MedicationStock", medicationStockId.toString());
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(error);
        }
        return ResponseEntity.ok(toResource(stock.get()));
    }

    @GetMapping("/citizen/{personUnderCareId}")
    @Operation(
            summary = "List the medication stocks of a person under care",
            description = "Retrieves the stock of every medication taken by the person under care, ordered by name."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Medication stocks retrieved successfully")
    })
    public ResponseEntity<List<MedicationStockResource>> getMedicationStocksByPersonUnderCareId(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId
    ) {
        var query = new GetMedicationStocksByPersonUnderCareIdQuery(new PersonUnderCareId(personUnderCareId));
        return ResponseEntity.ok(medicationStockQueryService.handle(query).stream()
                .map(this::toResource)
                .toList());
    }

    private MedicationStockResource toResource(MedicationStock stock) {
        return MedicationStockResourceFromEntityAssembler.toResourceFromEntity(
                stock, LocalDate.now(zone), stock.requiresRestock(restockThreshold));
    }
}
