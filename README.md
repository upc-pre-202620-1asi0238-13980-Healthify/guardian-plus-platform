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
| `careroutineswellness` | Care routines, medication reminders, medication stock and wearable wellness data (activity and sleep) | `/api/v1/reminders`, `/api/v1/medication-stocks` |
| `shared` | Shared kernel: i18n, result wrappers, persistence naming strategy, OpenAPI config | —                                                |

## Getting Started

### Prerequisites

- JDK 26
- Maven (or use the bundled `./mvnw` / `mvnw.cmd` wrapper)
- Docker and Docker Compose

### 1. Start the database

The `docker-compose.dev.yml` file starts a **PostgreSQL 17** instance and **pgAdmin 4** for local development:

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

In the `prod` profile, `DATABASE_URL`, `DATABASE_NAME`, `DATABASE_USER` and `DATABASE_PASSWORD` are required.

The schema is currently managed by Hibernate (`ddl-auto=update`).

### Emergency & Alerting

| Property | Description | Default |
|---|---|---|
| `emergency-alerting.scheduler.fall-confirmation-check-delay-ms` | How often detected falls are checked for confirmation (20 s window) | `1000` |
| `emergency-alerting.scheduler.ack-timeout-check-delay-ms` | How often unacknowledged alerts are checked for escalation | `5000` |
| `emergency-alerting.notifications.executor.pool-size` | Threads used to send notifications | `4` |
| `emergency-alerting.api.allow-any-alert-source` | Allow any alert source in `POST /api/v1/alerts`, not only falls and SOS | `true` in `dev`, `false` otherwise |

Push and SMS notifications are simulated (logged) until their providers are integrated; in-app alerts are served through `GET /api/v1/alerts/pending/recipient/{userId}`.

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
- **CD** (`.github/workflows/deploy.yml`): every push to `main` runs the tests, publishes the Docker image to GHCR and redeploys it on the Azure VM.

The VM runs `deploy/compose.yaml` (the API behind Caddy, which handles HTTPS) from `~/guardian-plus`, with its settings in `~/guardian-plus/.env` (see `deploy/.env.example`). The database is Azure Database for PostgreSQL.

The workflow needs the repository variables `VM_HOST` and `VM_USER` and the secret `VM_SSH_PRIVATE_KEY`.

## Contributing

1. Create a branch from `develop` using the `type/description` convention (e.g. `feat/care-routines-and-wellness`, `docs/database-setup`).
2. Use [Conventional Commits](https://www.conventionalcommits.org/) for commit messages (`feat(...)`, `fix(...)`, `docs(...)`, etc.).
3. Open a Pull Request targeting `develop` for review.
