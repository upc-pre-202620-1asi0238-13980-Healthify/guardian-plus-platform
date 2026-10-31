package com.healthify.guardian.platform.emergencyalerting.interfaces.rest;

import com.healthify.guardian.platform.emergencyalerting.application.commandservices.AlertCommandService;
import com.healthify.guardian.platform.emergencyalerting.domain.model.aggregates.Alert;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.resources.DeliveryStatusCallbackResource;
import com.healthify.guardian.platform.emergencyalerting.interfaces.rest.transform.RegisterDeliveryResultCommandFromResourceAssembler;
import com.healthify.guardian.platform.shared.application.result.ApplicationError;
import com.healthify.guardian.platform.shared.application.result.Result;
import com.healthify.guardian.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Webhook through which notification providers report the outcome of each delivery.
 */
@RestController
@RequestMapping(value = "/api/v1/webhooks/notification-deliveries", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Notification Delivery Webhook", description = "Delivery outcomes reported by notification providers")
public class NotificationDeliveryWebhookController {

    private final AlertCommandService alertCommandService;

    public NotificationDeliveryWebhookController(AlertCommandService alertCommandService) {
        this.alertCommandService = alertCommandService;
    }

    @PostMapping
    @Operation(
            summary = "Report a delivery outcome",
            description = "Records whether a delivery was sent, reached the device or failed. Repeated or late " +
                    "callbacks are accepted and ignored."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Outcome recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or unknown delivery"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    public ResponseEntity<?> reportDeliveryOutcome(@Valid @RequestBody DeliveryStatusCallbackResource resource) {
        var result = alertCommandService.handle(
                RegisterDeliveryResultCommandFromResourceAssembler.toCommandFromResource(resource));
        if (result instanceof Result.Failure<Alert, ApplicationError> failure) {
            return ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        }
        return ResponseEntity.noContent().build();
    }
}
