package com.healthify.guardian.platform.iam.domain.model.commands;

/**
 * Command to validate the e-mail and password of a login attempt.
 */
public record LoginCommand(String email, String rawPassword) {
}
