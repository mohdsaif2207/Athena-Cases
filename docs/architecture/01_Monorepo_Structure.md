# 01 — Complete Monorepo Folder Structure

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)

---

## 1. Full hierarchy

```
Athena-Cases/
│
├── .github/
│   ├── workflows/                    # CI: build backend, lint/build frontend, Flyway validate
│   └── PULL_REQUEST_TEMPLATE/        # PR checklist (standards + ownership)
│
├── api/                              # API CONTRACTS (language-agnostic)
│   ├── openapi/                      # openapi-v1.yaml (+ fragments if split later)
│   └── examples/                     # Sample request/response JSON for docs & mocks
│
├── cases-MT/                         # BACKEND (Spring Boot)  ← alias: backend/
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/
│       ├── main/
│       │   ├── java/com/athena/cases/
│       │   │   ├── CasesMtApplication.java
│       │   │   ├── common/           # Shared kernel (entities, DTOs, exceptions, utils)
│       │   │   ├── config/           # Spring configuration only
│       │   │   ├── security/         # AuthN/AuthZ filters, JWT, method security
│       │   │   ├── audit/            # Audit service wiring + listeners
│       │   │   ├── casemanagement/   # CaseManagementService port (+ later impl)
│       │   │   ├── workflow/         # WorkflowService port
│       │   │   ├── notification/     # NotificationService port
│       │   │   ├── lookup/           # LookupService port
│       │   │   ├── permission/       # PermissionService port
│       │   │   ├── filestorage/      # FileStorageService port
│       │   │   ├── client/           # Outbound HTTP clients (httpx-style RestClient wrappers)
│       │   │   └── features/         # FEATURE MODULES (one package per case type)
│       │   │       ├── _template/    # Copy-me skeleton (no business logic)
│       │   │       ├── billing/      # Dev 2 — empty until unlocked
│       │   │       ├── dbm/          # Dev 3 — empty until unlocked
│       │   │       └── exrt/         # Dev 4 — empty until unlocked
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-qa.yml
│       │       ├── application-uat.yml
│       │       ├── application-pci.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/
│       │           ├── common/       # Lead-owned shared tables
│       │           ├── lookups/      # Lead-owned lookup DDL (if any local)
│       │           ├── audit/        # Lead-owned audit tables
│       │           └── features/     # Per-feature migrations (ownership ranges)
│       └── test/java/com/athena/cases/
│
├── cases-UI/                         # FRONTEND (React)  ← alias: frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── app/                      # App bootstrap / providers
│       ├── layouts/                  # Shell layouts (app chrome)
│       ├── pages/                    # Non-feature top-level pages (login, forbidden)
│       ├── components/               # Shared presentational components
│       ├── hooks/                    # Shared hooks
│       ├── services/                 # Thin wrappers (optional; prefer api/)
│       ├── api/                      # Axios client + interceptors
│       ├── contexts/                 # Auth / theme contexts
│       ├── routes/                   # Route table + guards
│       ├── store/                    # UI-only global state (rare)
│       ├── theme/                    # MUI theme tokens
│       ├── assets/
│       ├── types/                    # Shared TS types
│       ├── utils/
│       ├── constants/
│       ├── validation/               # Shared Zod helpers
│       └── features/                 # FEATURE MODULES (mirrors backend ownership)
│           ├── _template/
│           ├── cases/                # Shared Cases List / New Case popup (Lead or Cases Search)
│           ├── billing-department-request/   # Dev 2
│           ├── dbm-work-order-request/       # Dev 3
│           └── exrt-request/                 # Dev 4
│
├── database/                         # HUMAN-FACING DB ARTIFACTS
│   ├── flyway/                       # Optional mirrored SQL for review (or README pointing to resources)
│   ├── seeds/                        # Non-prod seed data scripts
│   └── README.md                     # Naming, ownership, conflict avoidance
│
├── docs/
│   ├── architecture/                 # This blueprint set
│   ├── guides/                       # Day-to-day developer guides
│   ├── templates/                    # PR / ADR / module checklist templates
│   └── adr/                          # Architecture Decision Records
│
├── scripts/
│   ├── local/                        # bootstrap-db, run-all, format
│   └── ci/                           # helpers invoked by GitHub Actions
│
├── .gitignore
└── README.md
```

---

## 2. Responsibility of every top-level folder

| Folder | Responsibility | Conflict isolation |
|--------|----------------|--------------------|
| `.github/` | CI pipelines, required checks, PR template enforcing ownership | Lead only changes workflows |
| `api/` | Published OpenAPI — frontend & backend agree on contracts before coding | Path prefixes per feature reduce collisions |
| `cases-MT/` | Single Spring Boot deployable; package modularization | Features only edit `features/<own>` + own Flyway files |
| `cases-UI/` | Single SPA; feature folders isolate UI work | Features only edit `features/<own>` |
| `database/` | Docs, seeds, ownership notes; not a second migration runner | Prevents “mystery SQL” outside Flyway |
| `docs/` | Living architecture & onboarding | Doc PRs can parallelize |
| `scripts/` | Automate boring setup; never hold secrets | Lead-owned |

---

## 3. Why not multi-module Maven (yet)?

**Decision (A-F09):** Start as **one Maven module** with clear packages.

| Pros now | Cons / when to split |
|----------|----------------------|
| Faster for 4 developers; one Spring context | Later: extract `cases-common` JAR if compile-time isolation needed |
| Simpler CI | Multi-module when team > ~15 or deployables diverge |

If compile-time enforcement is required later, ADR: introduce `cases-common` + `cases-app` + optional feature jars. Until then, **ArchUnit** tests (foundation Phase 5) enforce “features must not depend on features.”

---

## 4. Dependency direction (backend)

```
features.* ──► ports (casemanagement, workflow, …) ──► common
     │                      │
     │                      ▼
     └──────────────► config / security (via Spring, not feature→feature)
```

Forbidden:

```
features.billing ──X──► features.dbm
features.exrt    ──X──► features.billing
```
