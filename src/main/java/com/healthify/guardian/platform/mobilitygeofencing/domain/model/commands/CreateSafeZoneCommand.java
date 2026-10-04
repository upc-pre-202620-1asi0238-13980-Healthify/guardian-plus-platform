package com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Coordinates;

import java.util.UUID;

public record CreateSafeZoneCommand(
        UUID fragileCitizenId,
        String name,
        Coordinates center,
        Double radiusInMeters
) {
    public CreateSafeZoneCommand {
        if (fragileCitizenId == null) throw new IllegalArgumentException("fragileCitizenId no puede ser nulo.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("El nombre de la geocerca es requerido.");
        if (center == null) throw new IllegalArgumentException("Las coordenadas del centro son requeridas.");
        if (radiusInMeters == null || radiusInMeters <= 0) throw new IllegalArgumentException("El radio debe ser mayor a 0 metros.");
    }
}
