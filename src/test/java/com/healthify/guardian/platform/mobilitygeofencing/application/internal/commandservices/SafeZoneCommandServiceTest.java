package com.healthify.guardian.platform.mobilitygeofencing.application.internal.commandservices;

import com.healthify.guardian.platform.mobilitygeofencing.domain.model.aggregates.SafeZone;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.ActivateSafeZoneCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.CreateSafeZoneCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.DeactivateSafeZoneCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.DeleteSafeZoneCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.commands.UpdateSafeZoneCommand;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.Coordinates;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.FragileCitizenId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.model.valueobjects.SafeZoneId;
import com.healthify.guardian.platform.mobilitygeofencing.domain.repositories.SafeZoneRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests of {@link SafeZoneCommandService} with an in-memory repository.
 */
class SafeZoneCommandServiceTest {

    /** Keeps the safe zones in insertion order, like the JPA adapter returns them. */
    private static class InMemorySafeZoneRepository implements SafeZoneRepository {
        private final Map<SafeZoneId, SafeZone> zones = new LinkedHashMap<>();

        @Override
        public SafeZone save(SafeZone safeZone) {
            zones.put(safeZone.getId(), safeZone);
            return safeZone;
        }

        @Override
        public Optional<SafeZone> findById(SafeZoneId id) {
            return Optional.ofNullable(zones.get(id));
        }

        @Override
        public Optional<SafeZone> findActiveByFragileCitizenId(FragileCitizenId fragileCitizenId) {
            return findAllByFragileCitizenId(fragileCitizenId).stream().filter(SafeZone::isActive).findFirst();
        }

        @Override
        public List<SafeZone> findAllByFragileCitizenId(FragileCitizenId fragileCitizenId) {
            return zones.values().stream().filter(zone -> zone.getFragileCitizenId().equals(fragileCitizenId)).toList();
        }

        @Override
        public void delete(SafeZoneId id) {
            zones.remove(id);
        }
    }

    private final InMemorySafeZoneRepository repository = new InMemorySafeZoneRepository();
    private final List<Object> events = new ArrayList<>();
    private final SafeZoneCommandService service = new SafeZoneCommandService(repository, events::add);
    private final UUID person = UUID.randomUUID();

    private SafeZoneId create(String name) {
        return service.handle(new CreateSafeZoneCommand(person, name, new Coordinates(-12.05, -77.04), 200.0));
    }

    private List<String> activeZoneNames() {
        return repository.findAllByFragileCitizenId(new FragileCitizenId(person)).stream()
                .filter(SafeZone::isActive).map(SafeZone::getName).toList();
    }

    @Test
    void aNewSafeZoneReplacesTheActiveOne() {
        create("Casa");
        create("Parque");

        assertThat(activeZoneNames()).containsExactly("Parque");
        assertThat(service.getAllByFragileCitizenId(new FragileCitizenId(person))).hasSize(2);
    }

    @Test
    void activatingAZoneDeactivatesTheOneThatWasActive() {
        var home = create("Casa");
        create("Parque");

        assertThat(service.handle(new ActivateSafeZoneCommand(home.value()))).isTrue();

        assertThat(activeZoneNames()).containsExactly("Casa");
        assertThat(service.getActiveByFragileCitizenId(new FragileCitizenId(person)))
                .get().extracting(SafeZone::getId).isEqualTo(home);
    }

    @Test
    void anotherPersonsActiveZoneIsNotTouched() {
        var otherPerson = UUID.randomUUID();
        service.handle(new CreateSafeZoneCommand(otherPerson, "Su casa", new Coordinates(-12.1, -77.0), 100.0));

        create("Casa");

        assertThat(service.getActiveByFragileCitizenId(new FragileCitizenId(otherPerson))).isPresent();
    }

    @Test
    void commandsOnAZoneThatDoesNotExistReportItInsteadOfThrowing() {
        var missing = UUID.randomUUID();

        assertThat(service.handle(new UpdateSafeZoneCommand(missing, "X", new Coordinates(-12.0, -77.0), 100.0))).isFalse();
        assertThat(service.handle(new ActivateSafeZoneCommand(missing))).isFalse();
        assertThat(service.handle(new DeactivateSafeZoneCommand(missing))).isFalse();
        assertThat(service.handle(new DeleteSafeZoneCommand(missing))).isFalse();
    }

    @Test
    void deactivatingTheActiveZoneLeavesNoneActive() {
        var home = create("Casa");

        assertThat(service.handle(new DeactivateSafeZoneCommand(home.value()))).isTrue();

        assertThat(service.getActiveByFragileCitizenId(new FragileCitizenId(person))).isEmpty();
    }
}
