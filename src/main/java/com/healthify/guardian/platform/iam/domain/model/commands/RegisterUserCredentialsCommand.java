package com.healthify.guardian.platform.iam.domain.model.commands;

/**
 * Command to register the credentials of a new caregiver or family member. The raw password only travels as far as
 * the {@code PasswordHasher}; the aggregate receives its hash.
 */
public record RegisterUserCredentialsCommand(String email, String rawPassword) {
}
