package com.healthify.guardian.platform.iam.application.commandservices;

import com.healthify.guardian.platform.iam.application.outboundservices.SessionToken;

import java.util.UUID;

/**
 * Result of a sign-in that does not know the account beforehand, like the Google one: the account that signed in
 * and its session token.
 */
public record AuthenticatedSession(UUID userAccountId, SessionToken token) {
}
