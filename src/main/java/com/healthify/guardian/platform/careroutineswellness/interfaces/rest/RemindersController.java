package com.healthify.guardian.platform.careroutineswellness.interfaces.rest;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.application.queryservices.ReminderQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetRemindersByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ReminderResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ScheduleReminderResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ReminderResourceFromEntityAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ScheduleReminderCommandFromResourceAssembler;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for routine reminder endpoints.
 */
@RestController
@RequestMapping(value = "/api/v1/reminders", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Reminders", description = "Routine reminder scheduling and lifecycle endpoints")
public class RemindersController {

    private final ReminderCommandService reminderCommandService;
    private final ReminderQueryService reminderQueryService;

    public RemindersController(ReminderCommandService reminderCommandService, ReminderQueryService reminderQueryService) {
        this.reminderCommandService = reminderCommandService;
        this.reminderQueryService = reminderQueryService;
    }

    @PostMapping
    @Operation(
            summary = "Schedule a reminder",
            description = "Schedules a new medication, appointment, physical activity or hydration reminder for a person under care."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reminder scheduled successfully",
                    content = @Content(schema = @Schema(implementation = ReminderResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> scheduleReminder(@Valid @RequestBody ScheduleReminderResource resource) {
        var command = ScheduleReminderCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = reminderCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ReminderResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @PutMapping("/{reminderId}/confirm")
    @Operation(
            summary = "Confirm a reminder",
            description = "Confirms a reminder that has already been issued (or reissued) to the person under care."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reminder confirmed successfully",
                    content = @Content(schema = @Schema(implementation = ReminderResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Reminder not found"),
            @ApiResponse(responseCode = "422", description = "Reminder cannot be confirmed in its current status")
    })
    public ResponseEntity<?> confirmReminder(
            @PathVariable
            @Parameter(description = "Reminder unique identifier", required = true)
            UUID reminderId
    ) {
        var result = reminderCommandService.handle(new ConfirmReminderCommand(reminderId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ReminderResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @DeleteMapping("/{reminderId}")
    @Operation(
            summary = "Cancel a reminder",
            description = "Cancels a reminder that is still scheduled, issued or reissued."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reminder cancelled successfully",
                    content = @Content(schema = @Schema(implementation = ReminderResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Reminder not found"),
            @ApiResponse(responseCode = "422", description = "Reminder cannot be cancelled in its current status")
    })
    public ResponseEntity<?> cancelReminder(
            @PathVariable
            @Parameter(description = "Reminder unique identifier", required = true)
            UUID reminderId
    ) {
        var result = reminderCommandService.handle(new CancelReminderCommand(reminderId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ReminderResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @GetMapping("/citizen/{personUnderCareId}")
    @Operation(
            summary = "List reminders for a person under care",
            description = "Retrieves every reminder scheduled for the given person under care."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reminders retrieved successfully")
    })
    public ResponseEntity<List<ReminderResource>> getRemindersByPersonUnderCareId(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId
    ) {
        var query = new GetRemindersByPersonUnderCareIdQuery(new PersonUnderCareId(personUnderCareId));
        var reminders = reminderQueryService.handle(query).stream()
                .map(ReminderResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(reminders);
    }
}
