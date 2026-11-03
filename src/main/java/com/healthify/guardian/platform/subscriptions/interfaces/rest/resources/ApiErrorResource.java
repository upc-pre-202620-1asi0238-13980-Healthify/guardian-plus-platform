package com.healthify.guardian.platform.subscriptions.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(
        description = "Standard error response returned by the Subscriptions API"
)
public record ApiErrorResource(

        Instant timestamp,

        int status,

        String error,

        String message,

        Map<String, String> details
) {

    public static ApiErrorResource validation(
            String message,
            Map<String, String> details
    ) {

        return new ApiErrorResource(
                Instant.now(),
                400,
                "Bad Request",
                message,
                details
        );
    }

    public static ApiErrorResource badRequest(
            String message
    ) {

        return new ApiErrorResource(
                Instant.now(),
                400,
                "Bad Request",
                message,
                Map.of()
        );
    }
}