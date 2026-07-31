# 07 — Database Strategy

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)  
**Engine:** PostgreSQL  
**Migrations:** Flyway (project stack)

---

## 1. Table categories

### Shared tables (Lead — `db/migration/common`)

| Table | Purpose |
|-------|---------|
| `cases` | Case header: type, number, owner, priority, status, assignee, parent, version, audit cols |
| `workflow_instances` | Workflow spine linked to `cases.id` |
| `notifications` | In-app / dashboard notifications linked to case |

### Audit tables (Lead — `db/migration/audit`)

| Table | Purpose |
|-------|---------|
| `audit_events` | Application-level audit trail (actor, action, entity, payload summary) |

*(Exact columns finalized in foundation Phase 3; do not invent business columns beyond LLD for features.)*

### Lookup tables (Lead / Platform — `db/migration/lookups`)

- Prefer **external** master data (A-F06).
- If local seed tables are required for stubs, Lead owns them; features store **IDs only**.

### Feature tables (Dev 2–4 — `db/migration/features`)

| Owner | Examples |
|-------|----------|
| Billing | `billing_department_requests`, `billing_request_hold_levels` |
| DBM | `dbm_*` (TBD by DBM LLD) |
| ExRT | `exrt_*` (TBD by ExRT LLD) |

**Pattern:** Feature extension **1:1** with `cases` via `case_id` FK; common `cases` has **no** FK back to feature tables.

```
cases (1) ──────── (1) billing_department_requests
  │
  └──── (1) ──── (N) billing_request_hold_levels
```

---

## 2. Naming conventions

| Object | Convention | Example |
|--------|------------|---------|
| Tables | `snake_case`, plural | `cases`, `notifications` |
| Columns | `snake_case` | `case_number`, `created_at` |
| PK | `id` BIGINT identity | |
| FK | `<ref>_id` | `case_id` |
| Indexes | `ix_<table>_<cols>` | `ix_cases_case_type` |
| Unique | `uq_<table>_<cols>` | `uq_cases_case_number` |
| Check | `chk_<table>_<col>` | `chk_cases_priority` |
| FK constraint | `fk_<table>_<col>` | `fk_billing_requests_case_id` |

**Types:** `TIMESTAMPTZ` for timestamps; `BOOLEAN` not Y/N; `VARCHAR(n)` always with length; enums as `VARCHAR` + `CHECK` unless PostgreSQL ENUM ADR approved.

**Audit on business tables:** `created_at`, `updated_at`, `created_by`, `updated_by`, `version`.

---

## 3. Flyway locations & versioning (conflict avoidance)

### Recommended layout

```
resources/db/migration/
  common/     V202607310900__common_create_cases.sql
  audit/      V202607310930__audit_create_audit_events.sql
  lookups/    V202607311000__lookups_stub_seed.sql   (optional)
  features/   V202607311200__billing_....sql
```

Configure Flyway to scan these folders (or flatten into one folder if simpler — **single version namespace** either way).

### Parallel developers without collisions

**Primary strategy — timestamp versions (preferred):**

```
V{yyyyMMddHHmm}__{owner}_{desc}.sql
```

- Developers generate version from UTC clock at authoring time.
- If two collide (rare): loser renames to `+1 minute` before merge.
- PR check: fail if duplicate version id.

**Secondary strategy — reserved ranges (optional):**

| Range | Owner |
|-------|-------|
| `V1`–`V999` | Reserved historical / bootstrap (Lead) |
| `V1000`–`V1999` | Lead common/audit/lookups |
| `V2000`–`V2999` | Billing |
| `V3000`–`V3999` | DBM |
| `V4000`–`V4999` | ExRT |
| `V9000`–`V9999` | Hotfixes (Lead only) |

Pick **one** strategy in ADR; do not mix casually. **Recommendation:** timestamps for greenfield speed + uniqueness.

### Rules

1. Never edit a migration already applied to shared `dev`/`qa`.
2. Never reuse a version number.
3. Every migration has a clear `owner:` header comment.
4. Feature migrations must not `ALTER` peer feature tables.
5. Altering `cases` requires Lead review (shared contract).

---

## 4. Migration authoring workflow

```
1. Pull latest develop
2. Create branch feature/<owner>-db-<desc>
3. Add ONLY your SQL file(s)
4. Run Flyway migrate locally
5. PR → develop (SQL review mandatory)
```

---

## 5. Seeds

- `database/seeds/` for non-prod demo data.
- Never run seeds in `prod`/`pci` automatically.
- Stub lookups may use seed scripts owned by Lead.

---

## 6. Rollback posture

Flyway undo is not assumed. Prefer:

- Forward-fix migrations.
- Expand/contract for breaking changes.
- Document manual rollback SQL in PR description when needed.
