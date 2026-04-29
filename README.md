# Remitly Stock Exchange

A simplified stock-exchange simulator built as a recruitment task. REST API for buying/selling single stocks between user wallets and a central bank, with a global audit log and a chaos endpoint that kills the serving instance - three app replicas behind HAProxy keep the service available.

## Highlights

- **Spring Boot 3.5 / Java 21** REST API with pessimistic locking; each trade is an atomic `bank update + wallet update + audit-log insert`.
- **PostgreSQL 16** as single source of truth, schema versioned by Flyway.
- **3 app instances behind HAProxy** - `POST /chaos` kills the serving instance; HAProxy removes it via `/actuator/health/readiness`, the other two keep serving, Docker restarts the fallen one.
- **One-command startup**: `./start.sh <PORT>`.
- **Cross-platform** (Linux / macOS / Windows, x64 + arm64) - only Docker is required.
- **Test pyramid**: unit (Mockito), slice (`@WebMvcTest` + MockMvc), E2E (Testcontainers + real Postgres).

## Quick start

Requires Docker and `docker compose`.

```bash
./start.sh 8080
```

Picks host port `8080` (change the argument otherwise). The script builds images, starts Postgres, three app replicas and HAProxy, and streams logs. Stop with `Ctrl+C` then `docker compose down`. Once HAProxy is ready the service is at `http://localhost:<PORT>`.

## REST API

All endpoints accept and return JSON. Errors: `{"error": "slug", "message": "human-readable description"}`.

| Method | Path | Purpose |
|---|---|---|
| `GET`  | `/stocks` | Bank inventory. |
| `GET`  | `/wallets/{wallet_id}` | Stocks held by a wallet. |
| `GET`  | `/wallets/{wallet_id}/stocks/{stock_name}` | Quantity of a single stock. `404` if never held. |
| `POST` | `/wallets/{wallet_id}/stocks/{stock_name}` | Buy or sell one unit. Body: `{"type":"buy"\|"sell"}`. Returns wallet state for that stock. |
| `GET`  | `/log` | Audit log, oldest first. Cursor-paginated: `?afterId=<id>&limit=<1..1000>` (defaults `0`, `100`). |
| `POST` | `/chaos` | Kill the serving instance (HA demo). Returns `202 Accepted`. |

### Error codes

| HTTP | `error` slug | When |
|---|---|---|
| `400` | `validation_failed` | Bean validation fails (e.g., missing `type`). |
| `400` | `malformed_request` | Body not parseable (e.g., unknown `type`). |
| `400` | `insufficient_stock` | Giving side has 0 (empty bank on buy, empty wallet on sell). |
| `404` | `stock_not_found` | No row in the bank for the named stock. |

### Examples

Bank inventory is seeded by Flyway on first boot (`AAPL`, `MSFT`, `GOOG`, `AMZN`, `NVDA` - 100 units each). Trading is the only write path - there is no hot-replace endpoint, which keeps the pessimistic-lock protocol race-safe. Edit `V3__seed_bank_inventory.sql` to change the starting catalogue.

```bash
# List the bank
curl http://localhost:8080/stocks

# Buy one AAPL for wallet "w1"
curl -X POST http://localhost:8080/wallets/w1/stocks/AAPL \
     -H 'Content-Type: application/json' \
     -d '{"type":"buy"}'
# => {"walletId":"w1","stockName":"AAPL","quantity":1}

# Read the wallet and the log
curl http://localhost:8080/wallets/w1
curl http://localhost:8080/log

# Kill the serving instance and watch HAProxy route around it
curl -X POST http://localhost:8080/chaos
curl http://localhost:8080/wallets/w1   # still answers, served by a surviving replica
```

## Architecture

```
          ┌──────────────┐
 client ──┤   HAProxy    ├── round-robin, /actuator/health/readiness checks
          └──────┬───────┘
                 │
     ┌───────────┼───────────┐
     │           │           │
 ┌───▼──┐    ┌───▼──┐    ┌───▼──┐
 │ app1 │    │ app2 │    │ app3 │   Spring Boot 3.5 / Java 21
 └───┬──┘    └───┬──┘    └───┬──┘
     └───────────┼───────────┘
                 │
           ┌─────▼──────┐
           │ Postgres16 │   single source of truth (ACID)
           └────────────┘
```

Each trade is one transaction: bank row locked first (`SELECT ... FOR UPDATE`), then wallet row, then audit insert. Same lock order everywhere prevents deadlocks between opposing concurrent trades. `spring.jpa.open-in-view=false` and `ddl-auto=validate` keep the runtime honest - Flyway owns the schema.

## Project layout

```
.
├── start.sh                 # one-command entrypoint (PORT argument)
├── docker-compose.yml       # postgres + 3 app replicas + haproxy
├── Dockerfile               # multi-stage: maven build → JRE runtime
├── haproxy/haproxy.cfg
├── pom.xml
└── src/
    ├── main/java/com/remitly/exchange/
    │   ├── chaos/           # InstanceKiller abstraction + System.exit impl
    │   ├── controller/      # REST endpoints
    │   ├── domain/          # JPA entities and ID classes
    │   ├── dto/             # record-based DTOs with validation
    │   ├── exception/       # domain exceptions + GlobalExceptionHandler
    │   ├── repository/      # Spring Data JPA with pessimistic-lock queries
    │   └── service/         # BankService, WalletService, TradingService, AuditLogService
    └── main/resources/
        ├── application.properties
        └── db/migration/    # Flyway - V1 schema, V2 audit timestamp, V3 seed data
```

## Development

Run the tests (requires Docker for Testcontainers):

```bash
./mvnw test
```

Covers service logic with Mockito, controller slices with MockMvc, and a full HTTP happy path against a Testcontainers-managed Postgres 16.

Run a single instance locally without Docker (expects Postgres on `localhost:5432`, user/password `exchange`):

```bash
./mvnw spring-boot:run
```

App env vars (all have defaults; override via shell env if needed):

| Variable | Default | Notes |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/exchange` | Postgres JDBC URL. |
| `SPRING_DATASOURCE_USERNAME` | `exchange` | DB user. |
| `SPRING_DATASOURCE_PASSWORD` | `exchange` | DB password. |
| `SERVER_PORT` | `8080` | Port the app listens on inside the container. |

Compose-only vars (read by `docker-compose.yml`):

| Variable | Default | Notes |
|---|---|---|
| `PORT` | `8080` | Host port HAProxy binds. Overridden by `./start.sh <PORT>`. |
| `POSTGRES_DB` | `exchange` | DB name. Propagated to the apps. |
| `POSTGRES_USER` | `exchange` | Postgres user. |
| `POSTGRES_PASSWORD` | `exchange` | Postgres password. |
