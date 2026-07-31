# 06 — Feature Module Template

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)

Every case type (Billing, DBM, ExRT, and future types) **must** follow this template. Copy `_template` packages; rename; fill behavior.

---

## 1. Backend template

```
com.athena.cases.features.<module>/
├── controller/     # REST endpoints for this case type only
├── service/        # Orchestration; injects shared ports + own repos
├── repository/     # Spring Data interfaces for feature tables
├── entity/         # JPA entities for extension tables (FK → cases.id)
├── dto/            # *Request / *Response records
├── mapper/         # MapStruct entity ↔ dto
├── validation/     # Feature-specific validators
├── client/         # Optional outbound calls unique to this feature
├── config/         # Optional @Configuration for feature beans
└── exception/      # Feature domain exceptions (extend common base)
```

### Naming

| Item | Convention | Example |
|------|------------|---------|
| Module package | kebab→snake short code | `billing`, `dbm`, `exrt` |
| Controller | `<Name>Controller` | `BillingDepartmentRequestController` |
| API base path | `/api/v1/<plural-kebab>` | `/api/v1/billing-department-requests` |
| Extension table | `<module>_...` snake | `billing_department_requests` |
| Case type code | UPPER_SNAKE | `BILLING_DEPARTMENT_REQUEST` |

### Mandatory service rules

1. Inject ports (`CaseManagementService`, …) — never peer features.
2. `@Transactional` on write methods; `readOnly = true` on queries.
3. Do not return entities from controllers.
4. On create: Case → extension → workflow → notification (order fixed unless ADR).
5. Unit tests: happy path + ≥1 negative (validation / not found / forbidden).

### Skeleton tests

```
src/test/java/com/athena/cases/features/<module>/
  <Name>ServiceTest.java
  <Name>ControllerTest.java
```

---

## 2. Frontend template

```
cases-UI/src/features/<case-type-kebab>/
├── pages/
├── components/
├── hooks/
├── api/
├── types/
├── validation/
├── routes.tsx
└── index.ts
```

### Mandatory UI rules

1. Modes: Create / View / Edit as required by story.
2. React Hook Form + Zod; React Query for server data.
3. `data-testid` = `<module>-<element>[-action]` kebab-case.
4. Loading / error / empty / loaded states.
5. No imports from sibling feature folders.

---

## 3. Flyway template (feature)

```
V{YYYYMMDDHHMM}__{owner}_{description}.sql
```

Example: `V202607311200__billing_create_billing_department_requests.sql`

Place under: `db/migration/features/` (or owner subfolder — see Database Strategy).

Header comment required:

```sql
-- owner: billing
-- author: <ad.username>
-- feature: Billing Department Request
```

---

## 4. New case-type onboarding (checklist)

- [ ] ADR or ticket for case type code + API path
- [ ] Copy backend `_template` → `features/<module>`
- [ ] Copy frontend `_template` → `features/<kebab>`
- [ ] Reserve Flyway version range / use timestamp naming
- [ ] Add case type to shared Cases popup **via Lead-owned list config** (not by editing peer features)
- [ ] OpenAPI paths added under `api/openapi`
- [ ] Permissions documented

---

## 5. Anti-patterns (refuse)

| Anti-pattern | Instead |
|--------------|---------|
| Shared “CaseDetails” table with 200 nullable columns | Extension table 1:1 with `cases` |
| Feature A calling Feature B service | Shared Case API / events |
| Hardcoded dropdown lists in controller | `LookupService` / feature lookup endpoints |
| CSV multi-select columns on new tables | Join table |
| Business logic in controller | Service layer |
