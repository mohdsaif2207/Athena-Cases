# 11 — Development Roadmap (Foundation First)

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)

Order is intentional: each phase removes blockers for the next and reduces rework for Dev 2–4.

---

## Phase 0 — Align (0.5 day)

- Walk through this architecture set with all four developers.
- Confirm assumptions A-F01…A-F10; open ADRs for disagreements.
- Confirm ownership matrix & Flyway strategy.

**Why first:** Prevents three divergent mental models.

---

## Phase 1 — Project skeleton hardening (1–2 days)

- Stabilize `cases-MT` / `cases-UI` tooling (Java 21, Spring Boot deps: Web, JPA, Validation, Security, Flyway, Lombok, MapStruct, Test).
- Add MUI, React Router, Axios, React Query, RHF, Zod to UI.
- Create package folders + `_template`.
- Root README + guides published.

**Why:** Empty Vite/Spring starters are not a foundation — dependencies and layout must exist before shared code.

---

## Phase 2 — Configuration & observability (1–2 days)

- `application.yml` + env profiles (`dev/qa/uat/pci/prod`) with key parity.
- Logging MDC filter (`requestId`).
- CORS for local UI.
- Actuator health (or `/api/v1/health`).
- `.env.example` (no secrets).

**Why:** Features should not invent their own config keys or logging style.

---

## Phase 3 — Database spine (2–3 days)

- Flyway wired.
- Create shared `cases`, `workflow_instances`, `notifications`, `audit_events` (minimal columns per LLD/shared needs).
- Document naming + migration ownership.
- Local Postgres via docker-compose script (optional but recommended).

**Why:** Feature extension tables need a stable `cases.id` FK target.

---

## Phase 4 — Security & permissions (2 days)

- Spring Security filter chain.
- JWT stub / IdP hook.
- `CurrentUserService`, `PermissionService` interfaces + stub.
- Method security enabled.
- Frontend AuthContext + ProtectedRoute stub.

**Why:** Controllers cannot be marked `@PreAuthorize` without a security baseline.

---

## Phase 5 — Shared ports & common types (2–3 days)

- `BaseEntity`, `AuditableEntity`, Api/Error DTOs, `GlobalExceptionHandler`.
- All shared service **interfaces** + **stubs**.
- MapStruct config; OpenAPI skeleton.
- ArchUnit: forbid feature→feature deps.

**Why:** Feature services must compile and unit-test against stable contracts.

---

## Phase 6 — Feature module unlock (ongoing)

- Dev 2 Billing, Dev 3 DBM, Dev 4 ExRT start in parallel.
- Each copies `_template`; owns API + UI + feature migrations.
- Lead supports shared Cases List / Case Type popup.

**Why:** Only safe after Phases 1–5; otherwise features fork incompatible patterns.

---

## Phase 7 — Testing & CI (overlaps late Phase 5–6)

- GitHub Actions: backend verify, frontend build, OpenAPI lint (optional).
- Testcontainers Postgres for integration tests.
- CODEOWNERS + PR template.
- Coverage gates as team agrees (don’t invent %).

**Why:** Parallel work needs automated conflict/quality signals.

---

## Phase 8 — Hardening (continuous)

- Replace stubs with real Workflow/Notification/Lookup adapters.
- Performance indexes from real query plans.
- Security review before `uat`/`pci`/`prod`.

---

## Recommended Gantt (ASCII)

```
Week 1:  [P0][P1========][P2====][P3========]
Week 2:  [P3==][P4========][P5============]
Week 3+: [P6 Billing | P6 DBM | P6 ExRT ][P7 CI]
```

---

## Exit criteria to start Phase 6

See [04_Shared_Foundation.md](./04_Shared_Foundation.md) §4 — Foundation ready.
