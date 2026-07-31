# Athena Cases — Enterprise Architecture Overview

**Document type:** Project Foundation Blueprint  
**Audience:** Lead developer + 3 parallel feature developers  
**Status:** Binding for foundation work  
**Last updated:** 2026-07-31  
**Scope:** Architecture & scaffolding only — **no case-type business features**

---

## Executive summary

Athena Cases is a greenfield **Case Management System** where many **case types** share one spine (Case, Workflow, Notification, Lookup, Security, Audit) and each case type is owned by a different developer.

| Goal | How this foundation achieves it |
|------|----------------------------------|
| Minimize merge conflicts | Vertical feature modules; no feature→feature dependencies; Flyway ownership ranges; package-per-feature |
| Loose coupling | Shared **ports (interfaces)** only; features depend on contracts, never on peer packages |
| Independent work | Dev 2/3/4 own one feature folder end-to-end (API + UI + migrations) |
| 10-year maintainability | Layered REST, stable API contracts, ADRs, coding standards, module template |

```
                    ┌─────────────────────────────────────┐
                    │           cases-UI (React)           │
                    │  shell + feature modules (isolated)  │
                    └─────────────────┬───────────────────┘
                                      │ HTTPS / JSON
                    ┌─────────────────▼───────────────────┐
                    │          cases-MT (Spring Boot)      │
                    │  common │ ports │ feature modules    │
                    └─────────────────┬───────────────────┘
                                      │ JPA / JDBC
                    ┌─────────────────▼───────────────────┐
                    │         PostgreSQL + Flyway          │
                    └─────────────────────────────────────┘
```

---

## Existing repo mapping (important)

The monorepo already used concrete module names. **Do not rename casually** (Maven artifact / npm package / CI paths). Treat them as aliases:

| Requested name | Actual folder | Role |
|----------------|---------------|------|
| `backend/` | `cases-MT/` | Spring Boot 3.x / Java 21 API |
| `frontend/` | `cases-UI/` | React + Vite + TypeScript UI |
| `database/` | `database/` + `cases-MT/src/main/resources/db/migration/` | Authoritative Flyway scripts live under backend resources; `database/` holds human-readable copies, seeds, and ownership notes |
| `docs/` | `docs/` | Architecture, guides, ADRs |
| `scripts/` | `scripts/` | Local + CI helper scripts |
| `api/` | `api/` | Versioned OpenAPI contracts (source of truth for consumers) |
| `.github/` | `.github/` | Workflows, PR template |

---

## Team ownership matrix

| Owner | Owns | May touch | Must NOT touch |
|-------|------|-----------|----------------|
| **Dev 1 (Lead)** | Foundation: `common`, `config`, `security`, `audit`, ports, Flyway **common/lookups/audit**, CI, docs standards | Cross-cutting fixes with PR review | Feature business rules inside Billing/DBM/ExRT |
| **Dev 2** | Billing Department Request | Own feature packages + own Flyway feature folder + own UI feature folder | Peer features; shared ports implementation without Lead |
| **Dev 3** | DBM Work Order Request | Same pattern for DBM | Peer features |
| **Dev 4** | ExRT Request | Same pattern for ExRT | Peer features |

**Hard rule:** Feature module A must never import Feature module B (backend packages or frontend feature folders).

---

## Document index (all 12 deliverables)

| # | Deliverable | Document |
|---|-------------|----------|
| 1 | Monorepo folder structure | [01_Monorepo_Structure.md](./01_Monorepo_Structure.md) |
| 2 | Spring Boot architecture | [02_Backend_Architecture.md](./02_Backend_Architecture.md) |
| 3 | React architecture | [03_Frontend_Architecture.md](./03_Frontend_Architecture.md) |
| 4 | Shared foundation | [04_Shared_Foundation.md](./04_Shared_Foundation.md) |
| 5 | Shared services (ports) | [05_Shared_Services.md](./05_Shared_Services.md) |
| 6 | Feature module template | [06_Feature_Module_Template.md](./06_Feature_Module_Template.md) |
| 7 | Database strategy | [07_Database_Strategy.md](./07_Database_Strategy.md) |
| 8 | API standards | [08_API_Standards.md](./08_API_Standards.md) |
| 9 | Coding standards | [09_Coding_Standards.md](./09_Coding_Standards.md) |
| 10 | Git strategy | [10_Git_Strategy.md](./10_Git_Strategy.md) |
| 11 | Development roadmap | [11_Development_Roadmap.md](./11_Development_Roadmap.md) |
| 12 | Documentation set | [../guides/](../guides/) |

---

## Assumptions (explicit — not invented as product behavior)

These fill gaps where LLDs / user stories do not yet decide platform-wide behavior:

| ID | Assumption | Impact if wrong |
|----|------------|-----------------|
| A-F01 | Root Java package remains `com.athena.cases` (existing app) | Rename is a coordinated foundation change |
| A-F02 | Migrations use **Flyway** (project stack); org Liquibase default does not apply here | Tooling/docs differ from other FMG modules |
| A-F03 | Auth is **JWT Bearer** via Spring Security; IdP details TBD by Lead | Security config remains stub until IdP chosen |
| A-F04 | Case number generation is owned by Case Management (shared), not by features | Features call `CaseManagementService` |
| A-F05 | Workflow / Notification are **ports** with provisional stubs until platform teams deliver | Features must not embed email/SMS SDKs |
| A-F06 | Lookup masters (clients, campaigns, …) are external / platform-owned; features store IDs only | No feature-owned master DDL |
| A-F07 | UI library is **MUI** as specified for this project (not Shadcn) | Shared UI components wrap MUI |
| A-F08 | Future case types (Coverage Amount, Report Request, Project Tracker) follow the same module template | No special-case architecture |
| A-F09 | One deployable backend JAR and one SPA; modularization is **package-level**, not multi-jar (unless later ADR) | Simpler CI for 4-dev team |
| A-F10 | Environments: `dev` / `qa` / `uat` / `pci` / `prod` (canonical tokens) | Config key parity across envs |

---

## Architectural principles

1. **Shared kernel, feature satellites** — Case header + ports are shared; case-type details live in extension tables/modules.
2. **Depend inward on ports** — Features call interfaces in `casemanagement`, `workflow`, `notification`, `lookup`, etc.
3. **No circular / peer feature deps** — Communication across case types goes through shared Case APIs only.
4. **Contract-first APIs** — OpenAPI under `api/openapi` updated with every public endpoint.
5. **Migration ownership** — File name includes owner prefix; version ranges reserved per developer.
6. **Fail fast** — Validation at API boundary; domain exceptions; global handler; no bare `RuntimeException`.
7. **Observable** — Structured logging with `requestId`, `userId`, `caseId`, `caseType` — never secrets/PII dumps.

---

## Next steps for Lead (Dev 1)

1. Read deliverables 01–11 end-to-end; open ADRs for any contested assumption.
2. Execute [11_Development_Roadmap.md](./11_Development_Roadmap.md) Phase 1–5 before unlocking feature branches for Dev 2–4.
3. Publish port stubs + OpenAPI skeleton; run a 30-minute onboarding walkthrough using [Developer_Onboarding_Guide.md](../guides/Developer_Onboarding_Guide.md).
