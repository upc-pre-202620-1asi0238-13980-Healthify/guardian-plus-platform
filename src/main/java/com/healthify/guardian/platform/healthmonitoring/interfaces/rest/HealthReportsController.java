package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.HealthReportCommandService;
import com.healthify.guardian.platform.healthmonitoring.application.queryservices.HealthReportQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetAllHealthReportsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetHealthReportByIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.HealthReportId;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.GenerateHealthReportResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.HealthReportResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.GenerateHealthReportCommandFromResourceAssembler;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.HealthReportResourceFromEntityAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.ErrorResource;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for health reports (US07, US19, US24).
 */
@RestController
@RequestMapping(value = "/api/v1/health-reports", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Health Reports", description = "On-demand and weekly health report endpoints")
public class HealthReportsController {

    private final HealthReportCommandService healthReportCommandService;
    private final HealthReportQueryService healthReportQueryService;

    public HealthReportsController(HealthReportCommandService healthReportCommandService,
                                   HealthReportQueryService healthReportQueryService) {
        this.healthReportCommandService = healthReportCommandService;
        this.healthReportQueryService = healthReportQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Generate a health report",
            description = "Compiles, on demand, the average, minimum, maximum and stability of every vital sign type " +
                    "of a care recipient within a period."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Report generated",
                    content = @Content(schema = @Schema(implementation = HealthReportResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid period", content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "No readings in the selected period", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> generateHealthReport(@Valid @RequestBody GenerateHealthReportResource resource) {
        var result = healthReportCommandService.handle(
                GenerateHealthReportCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, HealthReportResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get a health report", description = "Retrieves a compiled health report.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report found",
                    content = @Content(schema = @Schema(implementation = HealthReportResource.class))),
            @ApiResponse(responseCode = "404", description = "Report not found", content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getHealthReport(
            @PathVariable @Parameter(description = "Health report unique identifier", required = true) UUID reportId) {
        return healthReportQueryService.handle(new GetHealthReportByIdQuery(new HealthReportId(reportId)))
                .<ResponseEntity<?>>map(report -> ResponseEntity.ok(HealthReportResourceFromEntityAssembler.toResourceFromEntity(report)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("HealthReport", reportId.toString())));
    }

    @GetMapping("/care-recipient/{careRecipientProfileId}")
    @Operation(summary = "List health reports", description = "Lists every report of a care recipient, newest first.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HealthReportResource.class))))
    })
    public ResponseEntity<List<HealthReportResource>> getHealthReports(
            @PathVariable @Parameter(description = "Care recipient profile unique identifier", required = true)
            UUID careRecipientProfileId) {
        var query = new GetAllHealthReportsByCareRecipientProfileIdQuery(new CareRecipientProfileId(careRecipientProfileId));
        return ResponseEntity.ok(healthReportQueryService.handle(query).stream()
                .map(HealthReportResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
