package com.healthify.guardian.platform.healthmonitoring.domain.model.commands;

/**
 * Command to register a new vital sign type in the catalog.
 */
public record RegisterVitalSignTypeCommand(String code, String name, String unit) {
}
