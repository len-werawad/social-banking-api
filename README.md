# Social Banking API (Backend Developer Assignment)

**Social Banking API** is a comprehensive RESTful backend service for a modern mobile banking application. Built with Spring Boot, this API provides secure and efficient endpoints for managing banking operations including account management, transactions, authentication, and personalized financial dashboards.

The system follows a **modular monolithic architecture** using Spring Modulith principles, ensuring clear separation of concerns while maintaining the simplicity of a single deployable unit. It processes banking data from MySQL database.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running with Docker Compose](#running-with-docker-compose)
  - [Stopping Services](#stopping-services)
  - [Loading Mock Data](#loading-mock-data)
- [Environment Variables](#environment-variables)
- [Database: Migration & Mock Data](#database-migration--mock-data)
- [API Documentation (Swagger)](#api-documentation-swagger)
- [Authentication](#authentication)
- [Main Endpoints (v1)](#main-endpoints-v1)
  - [Auth](#auth)
  - [Dashboard](#dashboard)
  - [Accounts](#accounts)
  - [App configuration](#app-configuration)
  - [Response Success Format](#response-success-format)
  - [Response Error Format](#response-error-format)
- [Testing](#testing)
- [Module Diagram (Spring Modulith)](#module-diagram-spring-modulith)
- [Stress Test Report](#stress-test-report)
- [Improvements](#improvements)
- [Author](#author)

---

## Tech Stack

- **Java 21**
- **Spring Boot 3** (Web, Validation, Security, Actuator)
- **Spring Data JPA** + **MySQL 8**
- **Flyway** (DB migrations)
- **Redis** (caching)
- **JWT** (stateless auth)
- **OpenAPI / Swagger UI** (springdoc)

---

## Project Structure

```text
social-banking-api/
├── src/
│   ├── main/
│   │   ├── java/com/lbk/socialbanking/
│   │   │   ├── WalletApplication.java
│   │   │   ├── account/
│   │   │   ├── appconfig/
│   │   │   ├── auth/
│   │   │   ├── card/
│   │   │   ├── common/
│   │   │   ├── customer/
│   │   │   ├── dashboard/
│   │   │   └── transaction/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── db/migration/
│   │           ├── V1__schema.sql
│   │           └── V2__add_indexes_and_constraints.sql
│   └── test/
│       └── java/com/lbk/socialbanking/
│           ├── ModularityTest.java
│           └── ...
├── compose.yml
├── Dockerfile
├── build.gradle
└── docs/
    └── architecture/
        ├── components.puml
        └── components.png
```

All modules:
- `account` – account overview and related lists (accounts, goals, loans, payees)
- `appconfig` – app configuration (by environment/version/platform)
- `auth` – PIN login and token refresh (JWT)
- `card` – debit card data (design, status, details)
- `common` – shared infrastructure (security, web config, response envelope, errors)
- `customer` – customer profile and identity data
- `dashboard` – landing-page aggregation (greeting + key summaries)
- `transaction` – transaction lists and summaries (per account)

DB migrations:

- `src/main/resources/db/migration/`
  - `V1__schema.sql`
  - `V2__add_indexes_and_constraints.sql`

---

## Getting Started

### Prerequisites

**Required:**
- Docker Compose `v2.20+` or Podman Compose `v1.0+`

**Optional (for local development without Docker):**
- Java 21+
- Gradle 8.5+ (or use included `./gradlew`)

---

### Running with Docker Compose

1. Create `.env` file from the template (first time only):

```bash
cp .env.example .env
```

2. Start all services (MySQL + Redis + App):

```bash
docker compose up -d --build
# or: podman compose up -d --build
```

3. Wait for services to be healthy:

```bash
docker compose ps
```

API will be available at `http://localhost:8080`

---

### Stopping Services

```bash
# Stop all services
docker compose down

# Stop and remove volumes (⚠️ deletes all data)
docker compose down -v
```

---

### Loading Mock Data

After services are running, load the SQL dump:

```bash
docker compose exec -T mysql mysql -uapp -papp social_banking_db < sql_data_dump.sql
# or: podman compose exec -T mysql mysql -uapp -papp social_banking_db < sql_data_dump.sql
```

---

> **Note**: 
> - The `.env` file provides environment variables for container networking.
> - For local development without Docker, start only dependencies: `podman compose up mysql redis -d`, then run `./gradlew bootRun`

---

## Environment Variables

Spring reads configuration from `src/main/resources/application.yml`.

| Name | Default value | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring profile to activate. |
| `SERVER_PORT` | `8080` | HTTP port for the API server. |
| `LOG_LEVEL` | `ERROR` | Root logging level (`application-dev.yml` overrides to `INFO`). |
| `DB_URL` | `jdbc:mysql://localhost:3306/social_banking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | JDBC URL for MySQL. |
| `DB_USERNAME` | `app` | MySQL username used by the application. |
| `DB_PASSWORD` | `app` | MySQL password used by the application. |
| `DB_POOL_SIZE` | `20` | HikariCP max pool size. |
| `DB_POOL_MIN_IDLE` | `5` | HikariCP minimum idle connections. |
| `DB_CONNECTION_TIMEOUT` | `10000` | HikariCP connection timeout (ms). |
| `DB_IDLE_TIMEOUT` | `300000` | HikariCP idle timeout (ms). |
| `DB_MAX_LIFETIME` | `1500000` | HikariCP max lifetime (ms). |
| `FLYWAY_ENABLED` | `true` | Enable Flyway migrations on startup. |
| `REDIS_HOST` | `localhost` | Redis host for cache/token support. |
| `REDIS_PORT` | `6379` | Redis port. |
| `REDIS_TIMEOUT` | `2000ms` | Redis client timeout. |
| `JWT_SECRET` | `my-super-secret-jwt-key-32-bytes!!` | JWT signing secret (set a secure value in production). |
| `JWT_ISSUER` | `wallet-api` | JWT issuer claim (`iss`). |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `900000` | Access token expiration (ms). |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `604800000` | Refresh token expiration (ms). |

---

## Database: Migration & Mock Data

### Schema migrations (Flyway)

When the API starts with `FLYWAY_ENABLED=true`, Flyway runs migrations in:

- `classpath:db/migration`

### Mock data

This repository also includes a SQL dump file:

- `sql_data_dump.sql`

You can load it into the running MySQL container.

Example (Docker):

```bash
docker compose exec -T mysql mysql -uapp -papp social_banking_db < sql_data_dump.sql
```

Example (Podman):

```bash
podman compose exec -T mysql mysql -uapp -papp social_banking_db < sql_data_dump.sql
```
---

## API Documentation (Swagger)

Once the API is running:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Authentication

This API uses **JWT Bearer authentication** for most `/v1/**` endpoints.

### Public Endpoints (No Auth Required)

- `POST /v1/auth/login/pin` – Login with userId and PIN
- `POST /v1/auth/refresh` – Refresh access token
- `GET /v1/apps/config` – Get app configuration
- `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/**` – Documentation and monitoring

### Protected Endpoints

All other `/v1/**` endpoints require a valid JWT Bearer token in the `Authorization` header.

### Authentication Flow

1. **Login**: POST to `/v1/auth/login/pin` with credentials:
   ```json
   {
     "userId": "user123",
     "pin": "123456"
   }
   ```

2. **Receive tokens**:
   ```json
   {
     "data": {
       "accessToken": "eyJhbGciOiJIUzI1NiIs...",
       "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
       "expiresIn": 900000
     }
   }
   ```

3. **Use access token** in subsequent requests:
   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
   ```

4. **Refresh token** when access token expires:
   ```http
   POST /v1/auth/refresh
   Content-Type: application/json
   
   {
     "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
   }
   ```

> **Note**: Access tokens expire after 15 minutes (900,000 ms). Refresh tokens expire after 7 days (604,800,000 ms).

---

## Main Endpoints (v1)

### Auth

- `POST /v1/auth/login/pin`
- `POST /v1/auth/refresh`

### Dashboard

- `GET /v1/dashboards` (requires Bearer token)

### Accounts

- `GET /v1/accounts` (pagination: `page`, `limit`)
- `GET /v1/accounts/{accountId}/transactions` (cursor pagination: `cursor`, `limit`)
- `GET /v1/accounts/goals` (pagination: `page`, `limit`)
- `GET /v1/accounts/loans` (pagination: `page`, `limit`)
- `GET /v1/accounts/payees` (pagination: `page`, `limit`)

### App configuration

- `GET /v1/apps/config?environment=...&appVersion=...&platform=...`

For detailed schemas, use Swagger UI.


### Response Success Format
All list endpoints return paginated responses:

```json
{
  "data": [],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 100,
    "totalPages": 5
  }
}
```

### Response Error Format
All error responses follow this structure:

Including the __status__ field in the error response body improves clarity and reliability, especially when HTTP headers are unavailable or lost. As stated in RFC 9457 name-status, the status is advisory and helps consumers interpret the error even outside the HTTP context.

```json
{
  "error": {
    "status": 401,
    "code": "INVALID_USER",
    "message": "USER ID is incorrect.",
    "traceId": "abcd1234efgh5678"
  }
}
```

---

## Testing

Run unit tests:

```bash
./gradlew test
```

---

## Module Diagram (Spring Modulith)

This project uses **Spring Modulith** to enforce module boundaries and generate architecture diagrams.

- **Architecture Docs:** `build/spring-modulith-docs/`

### Generate / Update

Generate/update the PlantUML sources (runs `ModularityTest#createDocumentation()`):

```bash
./gradlew test --tests com.lbk.socialbanking.ModularityTest
```

Generated sources:
- `build/spring-modulith-docs/components.puml` (overall module diagram)
- `build/spring-modulith-docs/module-*.puml` (per-module diagrams)
- `build/spring-modulith-docs/all-docs.adoc` (aggregated docs)


## Stress Test Report

### Tool
- **k6**

### Test Scenario
- Warm-up → Normal load → Stress → Ramp down
- User flow: Login (PIN) → Dashboard

### Configuration
- **Peak load**: 100 Virtual Users
- **Duration**: ~3 minutes
- **Think time**: 100–400 ms (simulate real user behavior)

### Test Results
- [View K6 Stress Test Report](./docs/k6/summary.html)

**Key Metrics** (from latest test run):
- ✅ **Total Requests**: 15,234
- ✅ **Success Rate**: 99.9%
- ✅ **Avg Response Time**: 45ms
- ✅ **P95 Response Time**: 120ms
- ✅ **Max Virtual Users**: 100

To view the full interactive report locally:
```bash
open docs/k6/summary.html
```

---


## Improvements

Potential follow-ups to improve security and performance:

- **JWT revocation strategy**: implement refresh-token rotation (store `jti` in Redis) and optional denylist for forced logout.
- **Use BIGINT internal primary keys**: replace `VARCHAR` PK/FK with `BIGINT` to speed up joins and reduce index size.

---
## Author
**Werawad Ruangjaroon**
- Architecture & Implementation
- Contact: [GitHub](https://github.com/len-werawad)
- Blog: [Medium](https://medium.com/@len.werawad)

---
*Built with ❤️ using Spring Boot Modular Monolith architecture*