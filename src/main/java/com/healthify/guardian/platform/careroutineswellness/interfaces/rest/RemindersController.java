package com.healthify.guardian.platform.careroutineswellness.interfaces.rest;

import com.healthify.guardian.platform.careroutineswellness.application.commandservices.ReminderCommandService;
import com.healthify.guardian.platform.careroutineswellness.application.queryservices.ReminderQueryService;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.CancelReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.ConfirmReminderCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.commands.MarkReminderAsMissedCommand;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetReminderAdherenceQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetReminderStatusByIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.queries.GetRemindersByPersonUnderCareIdQuery;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.PersonUnderCareId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderId;
import com.healthify.guardian.platform.careroutineswellness.domain.model.valueobjects.ReminderType;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.AdherenceResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ReminderResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.resources.ScheduleReminderResource;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.AdherenceResourceFromSummaryAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ReminderResourceFromEntityAssembler;
import com.healthify.guardian.platform.careroutineswellness.interfaces.rest.transform.ScheduleReminderCommandFromResourceAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.infrastructure.i18n.MessageResolver;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for routine reminder endpoints: medication, medical appointments, light physical activity
 * and hydration.
 */
@RestController
@RequestMapping(value = "/api/v1/reminders", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Reminders", description = "Routine reminder scheduling, lifecycle and adherence endpoints")
public class RemindersController {

    /** Days covered by the adherence endpoint when no period is given: the last week, today included. */
    private static final int DEFAULT_ADHERENCE_DAYS = 7;
    private static final int MAX_ADHERENCE_DAYS = 366;

    private final ReminderCommandService reminderCommandService;
    private final ReminderQueryService reminderQueryService;
    private final ZoneId zone;

    public RemindersController(
            ReminderCommandService reminderCommandService,
            ReminderQueryService reminderQueryService,
            ZoneId careRoutinesWellnessZoneId) {
        this.reminderCommandService = reminderCommandService;
        this.reminderQueryService = reminderQueryService;
        this.zone = careRoutinesWellnessZoneId;
    }

    @PostMapping
    @Operation(
            summary = "Schedule a reminder",
            description = "Schedules a new medication, appointment, physical activity or hydration reminder for a " +
                    "person under care. A recurring reminder schedules its next occurrence each time one is issued."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Reminder scheduled successfully",
                    content = @Content(schema = @Schema(implementation = ReminderResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Linked medication stock not found for this person")
    })
    public ResponseEntity<?> scheduleReminder(@Valid @RequestBody ScheduleReminderResource resource) {
        var command = ScheduleReminderCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = reminderCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ReminderResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping("/{reminderId}")
    @Operation(summary = "Get a reminder", description = "Retrieves a single reminder occurrence.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reminder found",
                    content = @Content(schema = @Schema(implementation = ReminderResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Reminder not found")
    })
    public ResponseEntity<?> getReminderById(
            @PathVariable
            @Parameter(description = "Reminder unique identifier", required = true)
            UUID reminderId
    ) {
        var reminder = reminderQueryService.handle(new GetReminderStatusByIdQuery(new ReminderId(reminderId)));
        if (reminder.isEmpty()) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                    ApplicationError.notFound("Reminder", reminderId.toString()));
        }
        return ResponseEntity.ok(ReminderResourceFromEntityAssembler.toResourceFromEntity(reminder.get()));
    }

    @PutMapping("/{reminderId}/confirm")
    @Operation(
            summary = "Confirm a reminder",
            description = "Confirms a reminder that has already been issued (or reissued) to the person under care. " +
                    "A confirmed medication dose is discounted from its medication stock."
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

    @PutMapping("/{reminderId}/miss")
    @Operation(
            summary = "Mark a reminder as missed",
            description = "Closes an issued (or reissued) reminder the person under care never confirmed, once the " +
                    "family member has reviewed the omission. No dose is discounted from the medication stock."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reminder marked as missed",
                    content = @Content(schema = @Schema(implementation = ReminderResource.class))
            ),
            @ApiResponse(responseCode = "404", description = "Reminder not found"),
            @ApiResponse(responseCode = "422", description = "Reminder cannot be marked as missed in its current status")
    })
    public ResponseEntity<?> markReminderAsMissed(
            @PathVariable
            @Parameter(description = "Reminder unique identifier", required = true)
            UUID reminderId
    ) {
        var result = reminderCommandService.handle(new MarkReminderAsMissedCommand(reminderId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, ReminderResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @DeleteMapping("/{reminderId}")
    @Operation(
            summary = "Cancel a reminder",
            description = "Cancels a reminder that is still scheduled, issued or reissued. Cancelling the pending " +
                    "occurrence of a recurring reminder stops its series."
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
            description = "Retrieves the reminders scheduled for the given person under care, ordered by scheduled " +
                    "time, optionally narrowed to a period (ISO-8601 instants) and a type."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reminders retrieved successfully")
    })
    public ResponseEntity<List<ReminderResource>> getRemindersByPersonUnderCareId(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId,
            @RequestParam(required = false)
            @Parameter(description = "Only reminders scheduled at or after this instant, e.g. 2026-10-08T05:00:00Z")
            Instant from,
            @RequestParam(required = false)
            @Parameter(description = "Only reminders scheduled before this instant")
            Instant to,
            @RequestParam(required = false)
            @Parameter(description = "Only reminders of this type")
            ReminderType type
    ) {
        var query = new GetRemindersByPersonUnderCareIdQuery(new PersonUnderCareId(personUnderCareId), from, to, type);
        var reminders = reminderQueryService.handle(query).stream()
                .map(ReminderResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/citizen/{personUnderCareId}/adherence")
    @Operation(
            summary = "Get the adherence of a person under care",
            description = "Measures how many of the reminders that reached the person were confirmed, overall and " +
                    "day by day. Defaults to the last 7 days, today included."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Adherence measured successfully",
                    content = @Content(schema = @Schema(implementation = AdherenceResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid period")
    })
    public ResponseEntity<?> getAdherence(
            @PathVariable
            @Parameter(description = "Person under care unique identifier", required = true)
            UUID personUnderCareId,
            @RequestParam(required = false)
            @Parameter(description = "Only reminders of this type, e.g. MEDICATION")
            ReminderType type,
            @RequestParam(required = false)
            @Parameter(description = "First day of the period, e.g. 2026-10-02")
            LocalDate from,
            @RequestParam(required = false)
            @Parameter(description = "Last day of the period, e.g. 2026-10-08")
            LocalDate to
    ) {
        var lastDay = to != null ? to : LocalDate.now(zone);
        var firstDay = from != null ? from : lastDay.minusDays(DEFAULT_ADHERENCE_DAYS - 1);
        if (firstDay.isAfter(lastDay) || firstDay.plusDays(MAX_ADHERENCE_DAYS).isBefore(lastDay)) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.validationError(
                    "reminder-adherence",
                    MessageResolver.resolveOrDefault("reminder.adherence.period.invalid", "Invalid adherence period")));
        }
        var summary = reminderQueryService.handle(
                new GetReminderAdherenceQuery(new PersonUnderCareId(personUnderCareId), type, firstDay, lastDay));
        return ResponseEntity.ok(AdherenceResourceFromSummaryAssembler.toResourceFromSummary(summary));
    }
}
