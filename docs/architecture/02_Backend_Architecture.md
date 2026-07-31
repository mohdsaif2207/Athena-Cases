# 02 — Spring Boot Architecture

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)  
**Root package:** `com.athena.cases`

---

## 1. Package map

```
com.athena.cases
├── CasesMtApplication          # Boot entry — no business logic
│
├── common/                     # SHARED KERNEL (Lead)
│   ├── entity/                 # BaseEntity, AuditableEntity
│   ├── dto/                    # ApiResponse, ErrorResponse, PageResponse
│   ├── exception/              # Domain exceptions + GlobalExceptionHandler
│   ├── constants/              # Cross-cutting constants (header names, etc.)
│   ├── enums/                  # Shared enums (CaseStatus spine, Priority, …)
│   ├── util/                   # DateTimeUtils, RequestIdHolder helpers
│   └── validation/             # Reusable Bean Validation annotations
│
├── config/                     # Spring @Configuration only (Lead)
│   # Jackson, JPA auditing, OpenAPI, CORS, Flyway, Async, etc.
│
├── security/                   # AuthN/AuthZ (Lead)
│   # SecurityFilterChain, Jwt*, CurrentUser details bridge
│
├── audit/                      # Audit persistence + AuditService port usage
│
├── casemanagement/             # CaseManagementService (interface + later impl)
├── workflow/                   # WorkflowService
├── notification/               # NotificationService
├── lookup/                     # LookupService
├── permission/                 # PermissionService
├── filestorage/                # FileStorageService
│
├── client/                     # Outbound RestClient adapters (Lead / integration)
│
└── features/                   # CASE-TYPE MODULES (Dev 2–4)
    ├── _template/              # Canonical layout — copy for new types
    ├── billing/
    ├── dbm/
    └── exrt/
```

Each feature package (see deliverable 06) contains:

`controller | service | repository | entity | dto | mapper | validation | client | config | exception`

---

## 2. Purpose of every package

| Package | Purpose | Who changes |
|---------|---------|-------------|
| `common` | Types & behaviors reused by all modules; **no feature imports** | Lead |
| `config` | Wiring beans; profile-aware settings | Lead |
| `security` | Authentication, authorization annotations, permission constants | Lead |
| `audit` | Who changed what / when — cross-cutting | Lead |
| `casemanagement` | Create/link shared `cases` header; case number; version | Lead (+ stubs first) |
| `workflow` | Start/transition workflow instances | Lead (+ stubs) |
| `notification` | Team / user notifications | Lead (+ stubs) |
| `lookup` | Read-only reference data access | Lead (+ stubs) |
| `permission` | RBAC checks beyond annotations when needed | Lead |
| `filestorage` | Attachments abstraction (local/S3 later) | Lead |
| `client` | HTTP clients to other Athena services | Lead |
| `features.*` | Case-type vertical slices | Feature owners |

---

## 3. Layering inside a feature

```
Controller  →  validates HTTP + maps DTO
     ↓
Service     →  @Transactional orchestration; calls ports + repos
     ↓
Repository  →  Spring Data JPA only
     ↓
Entity      →  persistence model (never returned from controller)
```

**Rationale:** Controllers stay thin so UI contracts can change without rewriting business rules; repositories never call external systems; services own transactions and port calls.

---

## 4. Port vs adapter pattern

```
┌──────────────── features.billing.service ────────────────┐
│  BillingDepartmentRequestService                         │
│     uses CaseManagementService (interface)               │
│     uses WorkflowService (interface)                     │
│     uses NotificationService (interface)                 │
│     uses LookupService (interface)                       │
└──────────────────────────────────────────────────────────┘
                         │
                         ▼
┌──────────────── foundation packages ─────────────────────┐
│  *Service interfaces + Stub* / Real* implementations     │
└──────────────────────────────────────────────────────────┘
```

Feature code must inject **interfaces**, never concrete stubs (except tests). Spring binds the active implementation via `@Primary` / profile.

---

## 5. Configuration layout

```
src/main/resources/
  application.yml              # BASE — keys + ${ENV} placeholders; no secrets
  application-dev.yml          # overrides
  application-qa.yml
  application-uat.yml
  application-pci.yml
  application-prod.yml
```

- Profile selected at runtime: `SPRING_PROFILES_ACTIVE` (default `dev` for local).
- **Key parity:** every env file declares the same key set.
- Secrets: placeholders only (`${DB_PASSWORD}`), injected by secret manager / local `.env` (gitignored).

---

## 6. Testing package mirror

```
src/test/java/com/athena/cases/
  common/
  security/
  casemanagement/
  workflow/
  notification/
  lookup/
  features/_template/   # example test shapes for feature teams
  features/billing/     # owned by Dev 2 when unlocked
```

Use JUnit 5 + Mockito; WebMvcTest for controllers; `@DataJpaTest` for repositories; integration tests with Testcontainers (recommended foundation Phase 7).
