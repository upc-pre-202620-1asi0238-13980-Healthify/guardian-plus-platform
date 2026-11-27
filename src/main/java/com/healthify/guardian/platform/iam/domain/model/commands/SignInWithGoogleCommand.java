package com.healthify.guardian.platform.iam.domain.model.commands;

/**
 * Command to sign in with a Google ID token, creating the account the first time.
 */
public record SignInWithGoogleCommand(String idToken) {
}
