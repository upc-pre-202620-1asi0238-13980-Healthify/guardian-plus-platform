package com.healthify.guardian.platform.careroutineswellness.interfaces.rest;

import com.healthify.guardian.platform.careroutineswellness.application.queryservices.SleepCycleRecordQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetSleepCycleRecordsByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.SleepCycleRecordResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.SleepCycleRecordResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for the sleep cycles recorded by the wearable.
 */
@RestController
@RequestMapping(value = "/api/v1/sleep-cycle-records", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Sleep Cycle Records", description = "Nightly sleep history endpoints")
public class SleepCycleRecordsController {

    private final SleepCycleRecordQueryService sleepCycleRecordQueryService;

    public SleepCycleRecordsController(SleepCycleRecordQueryService sleepCycleRecordQueryService) {
        this.sleepCycleRecordQueryService = sleepCycleRecordQueryService;
    }

    @GetMapping("/citizen/{personUnderCareId}")
    @Operation(
            summary = "List the sleep cycles of a person under care",
            description = "Retrieves the closed nights of sleep, most recent first, with their duration, " +
                    "interruptions, continuity score and classification. Optionally narrowed to the nights that " +
                    "ended within a period (ISO-8601 instants)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sleep cycles retrieved successfully")
    })
    public ResponseEntity<List<SleepCycleRecordResource>> getSleepCycleRecordsByPersonUnderCareId(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId,
            @RequestParam(required = false)
            @Parameter(description = "Only nights that ended at or after this instant, e.g. 2026-10-01T00:00:00Z")
            Instant from,
            @RequestParam(required = false)
            @Parameter(description = "Only nights that ended before this instant")
            Instant to
    ) {
        var query = new GetSleepCycleRecordsByPersonUnderCareIdQuery(new PersonUnderCareId(personUnderCareId), from, to);
        return ResponseEntity.ok(sleepCycleRecordQueryService.handle(query).stream()
                .map(SleepCycleRecordResourceFromEntityAssembler::toResourceFromEntity)
                .toList());
    }
}
