package com.healthify.guardian.platform.profile.interfaces.rest.resources;

/**
 * Request payload to update a profile image.
 *
 * @param profileImageUrl the profile image URL
 */
public record UpdateProfileImageResource(
        String profileImageUrl
) {
}