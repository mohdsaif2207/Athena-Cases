# 04 — Shared Foundation (must exist before features)

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)

Nothing in Billing / DBM / ExRT should start until this checklist is **Done** or **Stubbed with approved interface**.

---

## 1. Foundation checklist

| Artifact | Package / location | Status target before feature unlock |
|----------|--------------------|-------------------------------------|
| `BaseEntity` | `common.entity` | Done — `id`, equals/hashCode strategy |
| `AuditableEntity` | `common.entity` | Done — `createdAt/By`, `updatedAt/By`, `version` |
| `ApiResponse<T>` | `common.dto` | Done |
| `ErrorResponse` | `common.dto` | Done — matches API error envelope |
| `PageResponse<T>` | `common.dto` | Done — pagination envelope |
| `GlobalExceptionHandler` | `common.exception` | Done |
| Domain exceptions | `common.exception` | Done — `ResourceNotFound`, `Validation`, `Conflict`, `Forbidden` |
| Validation framework | `common.validation` + Spring Validation | Done |
| Security framework | `security` | Stub JWT + method security hooks |
| Logging | Logback JSON / MDC | Done — `requestId`, `userId` |
| Constants / header names | `common.constants` | Done |
| Shared enums | `common.enums` | Done — Priority, CaseStatus spine, CaseType codes |
| Base repository patterns | Spring Data only (no custom base unless needed) | Documented |
| MapStruct config | `config` | Done — component model spring |
| Date utilities | `common.util` | Done — UTC/`Instant` helpers |
| Permission framework | `permission` + security constants | Stub |
| Flyway bootstrap | `db/migration/common` | Done — `cases` (+ audit) schema |
| OpenAPI skeleton | `api/openapi` | Done |
| ArchUnit dependency rules | tests | Done |
| Frontend Axios client | `cases-UI/src/api` | Done |
| Frontend Auth guard | `routes` + `contexts` | Stub |
| MUI theme | `theme` | Done |

---

## 2. Core type responsibilities

### BaseEntity
- Surrogate `Long id` (or UUID if ADR changes — default `Long` for Cases LLD).
- No business fields.

### AuditableEntity
- Extends `BaseEntity`.
- `createdAt`, `updatedAt` (`Instant`), `createdBy`, `updatedBy`, `@Version version`.
- Populated via Spring Data JPA auditing + `CurrentUserService`.

### ApiResponse / ErrorResponse
- Success and error envelopes (see [08_API_Standards.md](./08_API_Standards.md)).
- Controllers return DTOs wrapped consistently (or raw DTO with documented standard — **choose one in ADR**; recommended: explicit envelope for list/detail consistency).

### GlobalExceptionHandler
- Maps domain exceptions → HTTP status + `ErrorResponse`.
- Never leaks stack traces to clients in non-dev profiles.

### Security framework
- Authenticated principal available to services via `CurrentUserService`.
- `@PreAuthorize` on controllers; permission constants centralized.

### Permission framework
- Codes such as `CASES_CREATE`, `CASES_EDIT`, `CASES_VIEW` (exact list TBD with product — **assumption A-F03**).
- Features declare required permissions; Lead owns evaluation.

---

## 3. What is intentionally NOT in foundation

| Item | Why deferred |
|------|----------------|
| Billing / DBM / ExRT tables | Feature-owned |
| Real Workflow engine | Port + stub until platform ready |
| Real Notification channel | Port + stub |
| Master lookup DDL | Platform-owned (A-F06) |
| Full IdP integration | Stub JWT validator until IdP decided |

---

## 4. Definition of “foundation ready”

Lead can unlock feature developers when:

1. App boots with `dev` profile against local Postgres.
2. Flyway applies **common** migrations cleanly.
3. A sample secured `GET /api/v1/health` (or actuator) works.
4. Ports compile; stub implementations return deterministic test data.
5. Frontend boots with theme + router shell + Axios pointing at backend.
6. ArchUnit (or documented PR checklist) forbids feature→feature imports.
7. Docs 01–11 reviewed by all four developers (sign-off in onboarding).
