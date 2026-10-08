# Guardian+ Platform

Guardian+ is a caregiving platform by Healthify, built as a modular monolith with Spring Boot. It helps caregivers look after people under care by alerting their care circle when a fall, an SOS or another risk is detected, by managing care routines, medication reminders and medication stock, and by tracking wellness data such as activity and sleep from wearable devices.

## Tech Stack

- **Java 26** / **Spring Boot 4** (Web MVC, Data JPA, Validation)
- **PostgreSQL 17** as the primary datastore
- **springdoc-openapi** for API documentation (Swagger UI)
- **Lombok** to reduce boilerplate
- **Docker** / **Docker Compose** for the local database (PostgreSQL + pgAdmin)

## Architecture

The codebase follows a **Domain-Driven Design (DDD)**-inspired modular structure. Each bounded context under `com.healthify.guardian.platform` is organized into the same four layers:

```
<bounded-context>/
├── domain/           # Aggregates, value objects, commands, queries, events, repository interfaces
├── application/      # Command/query services, event handlers
├── infrastructure/   # JPA repositories, scheduling, messaging, configuration
└── interfaces/       # REST controllers, resources (DTOs) and assemblers
```

Cross-cutting concerns (result wrappers, i18n, persistence naming strategy, API documentation) live in the `shared` context.

### Bounded contexts

| Context | Responsibility | Base path                                        |
|---|---|--------------------------------------------------|
| `emergencyalerting` | Alerts raised by falls, SOS and other risk signals, their dispatch and escalation to the emergency contacts, and the incidents that follow | `/api/v1/alerts`, `/api/v1/incidents`, `/api/v1/alert-settings`, `/api/v1/emergency-contacts`, `/api/v1/alert-channel-settings` |
| `careroutineswellness` | Care routines, medication reminders, medication stock and wearable wellness data (activity and sleep) | `/api/v1/reminders`, `/api/v1/medication-stocks`, `/api/v1/hydration-plans`, `/api/v1/activity-monitors`, `/api/v1/sleep-cycle-records` |
| `shared` | Shared kernel: i18n, result wrappers, persistence naming strategy, OpenAPI config | —                                                |

## Getting Started

### Prerequisites

- JDK 26
- Maven (or use the bundled `./mvnw` / `mvnw.cmd` wrapper)
- Docker and Docker Compose

### 1. Start the database

The `docker-compose.dev.yml` file starts a **PostgreSQL 17** instance, **pgAdmin 4** and a **Mosquitto** MQTT broker for local development:

```bash
docker compose -f docker-compose.dev.yml up -d
```

Check that the containers are running:

```bash
docker compose -f docker-compose.dev.yml ps
```

| Service | Container | Host / URL | Credentials |
|---|---|---|---|
| PostgreSQL | `guardian-plus-postgres` | `localhost:5432`, database `guardian_plus` | `postgres` / `postgres` |
| pgAdmin | `guardian-plus-pgadmin` | `http://localhost:5050` | `admin@admin.com` / `admin` |
| Mosquitto | `guardian-plus-mosquitto` | `localhost:1883` (MQTT), `ws://localhost:9001` (WebSocket) | anonymous |

Data is persisted in the `guardian-plus-data` Docker volume, so it survives container restarts.

To connect pgAdmin to the database, register a new server with host `postgres` (the service name inside the Docker network), port `5432`, and username/password `postgres` / `postgres`.

Tables are created automatically when the application starts (`spring.jpa.hibernate.ddl-auto=update`), so no manual scripts are needed.

### 2. Run the application

Run the application (defaults to the `dev` profile):

```bash
# Linux / macOS
./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

The API will be available at `http://localhost:8080`, with interactive API docs at `http://localhost:8080/swagger-ui/index.html`.

In the `dev` profile the backend subscribes to the vital sign telemetry on `ws://localhost:9001`, so readings published by the [IoT simulator](https://github.com/upc-pre-202620-1asi0238-13980-Healthify/guardian-plus-iot-simulator) show up in the live view without extra setup.

### Run the whole stack in Docker

To run the backend and the IoT simulator as containers too, use the `full` profile. The simulator is built from a sibling checkout of `guardian-plus-iot-simulator` (`../../guardian-plus-iot-simulator`); set `SIMULATOR_DIR` if yours lives elsewhere.

```bash
docker compose -f docker-compose.dev.yml --profile full up -d --build
```

| Service | Container | Host / URL |
|---|---|---|
| Backend | `guardian-plus-backend` | `http://localhost:8080` |
| IoT simulator | `guardian-plus-simulator` | `http://localhost:5055` (port 5000 is taken by AirPlay on macOS) |

The simulator loads the wearable devices once at startup, so it waits until the backend is up. Stop everything with `docker compose -f docker-compose.dev.yml --profile full down`.

To reach the local backend from the mobile app on the emulator, point it to `http://10.0.2.2:8080/api/v1/` (the emulator's alias for the host machine; debug builds hold the local network permission Android 17 requires). On a physical phone over USB, use `http://127.0.0.1:8080/api/v1/` plus `adb reverse tcp:8080 tcp:8080`.

### Useful database commands

```bash
# Stop the containers (keeps the data)
docker compose -f docker-compose.dev.yml down

# Stop the containers and delete all database data
docker compose -f docker-compose.dev.yml down -v

# Follow PostgreSQL logs
docker compose -f docker-compose.dev.yml logs -f postgres

# Open a psql shell inside the container
docker exec -it guardian-plus-postgres psql -U postgres -d guardian_plus
```

## Configuration

### Profiles

Profiles live under `src/main/resources/`:

- `application-dev.properties` (default): local development, verbose SQL logging, defaults pointing to the Docker PostgreSQL instance.
- `application-prod.properties`: all database settings sourced from environment variables, SSL required (`sslmode=require`).

To run with a different profile:

```bash
# Linux / macOS
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

```powershell
# Windows (PowerShell)
$env:SPRING_PROFILES_ACTIVE="prod"; .\mvnw.cmd spring-boot:run
```

### Environment variables

| Variable | Description | Default (`dev`) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `DATABASE_URL` | PostgreSQL host | `localhost` |
| `DATABASE_PORT` | PostgreSQL port | `5432` |
| `DATABASE_NAME` | PostgreSQL database name | `guardian_plus` |
| `DATABASE_USER` | PostgreSQL username | `postgres` |
| `DATABASE_PASSWORD` | PostgreSQL password | `postgres` |
| `PORT` | Port the application listens on | `8080` |
| `HEALTH_MONITORING_MQTT_ENABLED` | Subscribe to the vital sign telemetry | `true` in `dev`, `false` otherwise |
| `HEALTH_MONITORING_MQTT_BROKER_URL` | MQTT broker, over WebSocket (`ws://` or `wss://`) | `ws://localhost:9001` |
| `HEALTH_MONITORING_MQTT_TOPIC` | Telemetry topic filter | `guardian/vitals/+` |
| `CARE_ROUTINES_WELLNESS_MQTT_ENABLED` | Subscribe to the activity (`guardian/activity/+`) and sleep (`guardian/sleep/+`) telemetry | `true` in `dev`, `false` otherwise |
| `CARE_ROUTINES_WELLNESS_MQTT_BROKER_URL` | MQTT broker, over WebSocket (`ws://` or `wss://`) | `ws://localhost:9001` |

In the `prod` profile, `DATABASE_URL`, `DATABASE_NAME`, `DATABASE_USER` and `DATABASE_PASSWORD` are required.

The schema is currently managed by Hibernate (`ddl-auto=update`).

`ddl-auto=update` adds columns and tables but never drops or updates constraints. A database created before the Care Routines & Wellness frontend alignment still has:

- a unique constraint on `medication_stocks.person_under_care_id`, which rejects a second medication for the same person;
- a `reminders_status_check` constraint without the `MISSED` status, which rejects marking a reminder as missed.

The simplest fix is to drop the context's old tables once and let Hibernate recreate them on the next start (this deletes their rows, so back them up first if they matter):

```sql
DROP TABLE IF EXISTS activity_monitors, medication_stocks, reminders, sleep_cycle_records;
```

### Emergency & Alerting

| Property | Description | Default |
|---|---|---|
| `emergency-alerting.scheduler.fall-confirmation-check-delay-ms` | How often detected falls are checked for confirmation (20 s window) | `1000` |
| `emergency-alerting.scheduler.ack-timeout-check-delay-ms` | How often unacknowledged alerts are checked for escalation | `5000` |
| `emergency-alerting.notifications.executor.pool-size` | Threads used to send notifications | `4` |
| `emergency-alerting.api.allow-any-alert-source` | Allow any alert source in `POST /api/v1/alerts`, not only falls and SOS | `true` in `dev`, `false` otherwise |

Push and SMS notifications are simulated (logged) until their providers are integrated; in-app alerts are served through `GET /api/v1/alerts/pending/recipient/{userId}`.

### Care Routines & Wellness

| Property | Description | Default |
|---|---|---|
| `care-routines-wellness.zone-id` | Zone the sleep window, daily reminders and adherence days are evaluated in | `America/Lima` |
| `care-routines-wellness.sleep-window.start` / `.end` | Sleep window: hydration reminders inside it are suppressed and inactivity is only watched outside of it | `22:00` / `07:00` |
| `care-routines-wellness.reminder.reissue-tolerance-minutes` | Minutes an issued medication reminder waits for confirmation before it is reissued | `10` |
| `care-routines-wellness.medication-stock.restock-threshold-days` | Days of supply at or below which a restock is suggested | `3` |
| `care-routines-wellness.medication-stock.doses-per-medication-confirmation` | Doses discounted from the stock per confirmed medication reminder | `1` |

Recurring reminders (`DAILY`, `WEEKLY`, `HOURLY`) are stored one occurrence at a time: the next occurrence is scheduled when the current one is issued, so adherence is measured dose by dose (`GET /api/v1/reminders/citizen/{id}/adherence`).

### Internationalization

Response messages are localized via `spring.messages.basename=messages`, with bundles for English (`messages.properties`) and Spanish (`messages_es.properties`).

## Testing

```bash
./mvnw test
```

## Building

```bash
./mvnw clean package
```

Produces an executable JAR under `target/`.

## Deployment

- **CI** (`.github/workflows/ci.yml`): builds and runs the tests on every PR to `develop` or `main`.
- **CD** (`.github/workflows/deploy.yml`): every push to `develop` runs the tests, publishes the Docker image to GHCR and redeploys it on the Azure VM.

The VM runs `deploy/compose.yaml` (the API behind Caddy, which handles HTTPS) from `~/guardian-plus`, with its settings in `~/guardian-plus/.env` (see `deploy/.env.example`). The database is Azure Database for PostgreSQL.

The workflow needs the repository variables `VM_HOST` and `VM_USER` and the secret `VM_SSH_PRIVATE_KEY`.

## Contributing

1. Create a branch from `develop` using the `type/description` convention (e.g. `feat/care-routines-and-wellness`, `docs/database-setup`).
2. Use [Conventional Commits](https://www.conventionalcommits.org/) for commit messages (`feat(...)`, `fix(...)`, `docs(...)`, etc.).
3. Open a Pull Request targeting `develop` for review.
