package com.healthify.guardian.platform.healthmonitoring.interfaces.rest;

import com.healthify.guardian.platform.healthmonitoring.application.commandservices.VitalSignCommandService;
import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignQueryService;
import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignThresholdQueryService;
import com.healthify.guardian.platform.healthmonitoring.application.queryservices.VitalSignTypeQueryService;
import com.healthify.guardian.platform.healthmonitoring.domain.model.commands.EmitVitalSignsCommand;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetAllVitalSignTypesQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetLiveVitalSignsByCareRecipientProfileIdQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.queries.GetVitalSignsByCareRecipientProfileIdAndPeriodQuery;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.CareRecipientProfileId;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.DateRange;
import com.healthify.guardian.platform.healthmonitoring.domain.model.valueobjects.VitalSignId;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.DetectVitalSignsResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.LiveVitalSignsResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.TelemetryBatchResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.TelemetryBatchResultResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.resources.VitalSignResource;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.DetectVitalSignsCommandFromResourceAssembler;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.LiveVitalSignsResourceFromEntityAssembler;
import com.healthify.guardian.platform.healthmonitoring.interfaces.rest.transform.VitalSignResourceFromEntityAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.interfaces.rest.resources.ErrorResource;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for vital sign telemetry ingestion and live/historical consultation.
 */
@RestController
@RequestMapping(value = "/api/v1/vital-signs", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Vital Signs", description = "Vital sign telemetry ingestion, live view and history endpoints")
public class VitalSignsController {

    private final VitalSignCommandService vitalSignCommandService;
    private final VitalSignQueryService vitalSignQueryService;
    private final VitalSignTypeQueryService vitalSignTypeQueryService;
    private final VitalSignThresholdQueryService vitalSignThresholdQueryService;
    private final Duration liveSignalWindow;

    public VitalSignsController(VitalSignCommandService vitalSignCommandService,
                                VitalSignQueryService vitalSignQueryService,
                                VitalSignTypeQueryService vitalSignTypeQueryService,
                                VitalSignThresholdQueryService vitalSignThresholdQueryService,
                                @Value("${health-monitoring.live.signal-window-seconds:60}") long liveSignalWindowSeconds) {
        this.vitalSignCommandService = vitalSignCommandService;
        this.vitalSignQueryService = vitalSignQueryService;
        this.vitalSignTypeQueryService = vitalSignTypeQueryService;
        this.vitalSignThresholdQueryService = vitalSignThresholdQueryService;
        this.liveSignalWindow = Duration.ofSeconds(liveSignalWindowSeconds);
    }

    @PostMapping
    @Operation(
            summary = "Detect a vital sign",
            description = "Registers a reading sent by an assigned wearable device. The reading is then emitted for the " +
                    "live view and evaluated against the threshold in force; consecutive out-of-range readings raise a " +
                    "vital sign anomaly for Emergency & Alerting."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vital sign detected",
                    content = @Content(schema = @Schema(implementation = VitalSignResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "404", description = "Wearable device or vital sign type not found",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "409", description = "The same reading was already stored",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "The device is not assigned to the care recipient",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> detectVitalSign(@Valid @RequestBody DetectVitalSignsResource resource) {
        var command = DetectVitalSignsCommandFromResourceAssembler.toCommandFromResource(resource, Instant.now());
        var result = vitalSignCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, VitalSignResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PostMapping("/batches")
    @Operation(
            summary = "Ingest a telemetry batch",
            description = "Ingests the readings buffered by a wearable while it was offline (TS02, US21). The batch is " +
                    "validated as a whole; readings already stored are skipped as duplicates."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Batch accepted",
                    content = @Content(schema = @Schema(implementation = TelemetryBatchResultResource.class))),
            @ApiResponse(responseCode = "400", description = "Malformed batch",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "A reading of the batch is invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> ingestBatch(@Valid @RequestBody TelemetryBatchResource resource) {
        var receivedAt = Instant.now();
        var commands = resource.readings().stream()
                .map(reading -> DetectVitalSignsCommandFromResourceAssembler.toCommandFromResource(reading, receivedAt))
                .toList();
        var result = vitalSignCommandService.handleBatch(commands);
        return ResponseEntityAssembler.toResponseEntityFromResult(result, stored -> new TelemetryBatchResultResource(
                commands.size(),
                stored.size(),
                commands.size() - stored.size(),
                stored.stream().map(VitalSignResourceFromEntityAssembler::toResourceFromEntity).toList()
        ), HttpStatus.ACCEPTED);
    }

    @PostMapping("/{vitalSignId}/emit")
    @Operation(
            summary = "Emit a vital sign",
            description = "Publishes a detected reading for the live view. Readings are emitted automatically after " +
                    "detection; this endpoint only re-triggers the step for a reading that was not emitted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vital sign emitted",
                    content = @Content(schema = @Schema(implementation = VitalSignResource.class))),
            @ApiResponse(responseCode = "404", description = "Vital sign not found",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class))),
            @ApiResponse(responseCode = "422", description = "Vital sign already emitted",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> emitVitalSign(
            @PathVariable @Parameter(description = "Vital sign unique identifier", required = true) UUID vitalSignId) {
        var result = vitalSignCommandService.handle(new EmitVitalSignsCommand(vitalSignId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, VitalSignResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @GetMapping("/{vitalSignId}")
    @Operation(summary = "Get a vital sign", description = "Retrieves a single stored reading.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vital sign found",
                    content = @Content(schema = @Schema(implementation = VitalSignResource.class))),
            @ApiResponse(responseCode = "404", description = "Vital sign not found",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<?> getVitalSign(
            @PathVariable @Parameter(description = "Vital sign unique identifier", required = true) UUID vitalSignId) {
        return vitalSignQueryService.findById(new VitalSignId(vitalSignId))
                .<ResponseEntity<?>>map(vitalSign -> ResponseEntity.ok(VitalSignResourceFromEntityAssembler.toResourceFromEntity(vitalSign)))
                .orElseGet(() -> ErrorResponseAssembler.toErrorResponseFromApplicationError(
                        ApplicationError.notFound("VitalSign", vitalSignId.toString())));
    }

    @GetMapping("/live/{careRecipientProfileId}")
    @Operation(
            summary = "Get live vital signs",
            description = "Retrieves the latest emitted reading of every vital sign type of a care recipient, " +
                    "classified against the threshold in force (US01-US05). When the latest reading is older than the " +
                    "live signal window it is still returned, flagged without live signal."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Live vital signs retrieved",
                    content = @Content(schema = @Schema(implementation = LiveVitalSignsResource.class)))
    })
    public ResponseEntity<LiveVitalSignsResource> getLiveVitalSigns(
            @PathVariable @Parameter(description = "Care recipient profile unique identifier", required = true)
            UUID careRecipientProfileId) {
        var recipient = new CareRecipientProfileId(careRecipientProfileId);
        var latest = vitalSignQueryService.handle(new GetLiveVitalSignsByCareRecipientProfileIdQuery(recipient));
        var types = vitalSignTypeQueryService.handle(new GetAllVitalSignTypesQuery());
        var thresholds = vitalSignThresholdQueryService.handle(
                new GetActiveVitalSignThresholdsByCareRecipientProfileIdQuery(recipient));
        return ResponseEntity.ok(LiveVitalSignsResourceFromEntityAssembler.toResourceFromEntities(
                careRecipientProfileId, latest, types, thresholds, liveSignalWindow, Instant.now()));
    }

    @GetMapping("/history/{careRecipientProfileId}")
    @Operation(
            summary = "Get vital sign history",
            description = "Retrieves the readings of a care recipient measured between two dates (inclusive, UTC), oldest first."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Readings retrieved",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = VitalSignResource.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid period",
                    content = @Content(schema = @Schema(implementation = ErrorResource.class)))
    })
    public ResponseEntity<List<VitalSignResource>> getVitalSignHistory(
            @PathVariable @Parameter(description = "Care recipient profile unique identifier", required = true)
            UUID careRecipientProfileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "First day, inclusive", example = "2026-09-28") LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Last day, inclusive", example = "2026-10-04") LocalDate to) {
        var query = new GetVitalSignsByCareRecipientProfileIdAndPeriodQuery(
                new CareRecipientProfileId(careRecipientProfileId), new DateRange(from, to));
        return ResponseEntity.ok(vitalSignQueryService.handle(query).stream()
                .map(VitalSignResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
