package com.healthify.guardian.platform.iam.domain.model.commands;

import java.util.UUID;

/**
 * Command to close a successful login and issue the signed session token (JWT) of the account.
 */
public record AuthenticateUserCommand(UUID userAccountId) {
}
