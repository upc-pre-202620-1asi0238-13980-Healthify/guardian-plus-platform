package com.healthify.guardian.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload of the Google sign-in: the ID token Google issued to the app for the platform's Web client.
 */
public record SignInWithGoogleResource(
        @NotBlank(message = "{user-account.google-token.required}")
        @Schema(example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
        String idToken
) {
}
