package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignTypeCommandService;
import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignTypeQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetAllVitalSignTypesQuery;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.RegisterVitalSignTypeResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignTypeResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.RegisterVitalSignTypeCommandFromResourceAssembler;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.VitalSignTypeResourceFromEntityAssembler;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.ErrorResource;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the vital sign type catalog.
 */
@RestController
@RequestMapping(value = "/api/v1/vital-sign-types", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Vital Sign Types", description = "Vital sign type catalog endpoints")
public class VitalSignTypesController {

    private final VitalSignTypeCommandService vitalSignTypeCommandService;
    private final VitalSignTypeQueryService vitalSignTypeQueryService;

    public VitalSignTypesController(VitalSignTypeCommandService vitalSignTypeCommandService,
                                    VitalSignTypeQueryService vitalSignTypeQueryService) {
        this.vitalSignTypeCommandService = vitalSignTypeCommandService;
        this.vitalSignTypeQueryService = vitalSignTypeQueryService;
    }

    @GetMapping
    @Operation(summary = "List vital sign types", description = "Lists the vital sign types supported by the platform.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Catalog retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = VitalSignTypeResource.class))))
    })
    public ResponseEntity<List<VitalSignTypeResource>> getAllVitalSignTypes() {
        return ResponseEntity.ok(vitalSignTypeQueryService.handle(new GetAllVitalSignTypesQuery()).stream()
                .map(VitalSignTypeResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }

    @PostMapping
    @Operation(summary = "Register a vital sign type", description = "Adds a vital sign type to the catalog (administrative use).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Type registered",
                    content = @Content(schema = @Schema(implementation = VitalSignTypeResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "409", description = "Code already registered", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> registerVitalSignType(@Valid @RequestBody RegisterVitalSignTypeResource resource) {
        var result = vitalSignTypeCommandService.handle(
                RegisterVitalSignTypeCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, VitalSignTypeResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }
}
