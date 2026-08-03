# 10 — Git Strategy

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)

---

## 1. Branch model

```
main          # production-ready only; protected
  ▲
develop       # integration branch; daily target for PRs
  ▲
feature/*     # developer work
bugfix/*
chore/*
docs/*
```

| Branch | Purpose | Protection |
|--------|---------|------------|
| `main` | Release / prod tags | No direct commits; PR + Lead approval |
| `develop` | Integration | PR required; CI green |
| `feature/<owner>-<ticket>-<slug>` | Feature work | Short-lived |
| `feature/foundation-*` | Lead foundation | Lead |

Examples:

- `feature/foundation-shared-ports`
- `feature/billing-INS123-create-api`
- `feature/dbm-INS456-schema`
- `feature/exrt-INS789-ui-form`

---

## 2. Pull request rules

1. PR into **`develop`** (not `main`).
2. Title: `[owner] short imperative summary` — e.g. `[billing] add hold-levels lookup endpoint`.
3. Description: intent, screenshots (UI), migration notes, risk.
4. Touch **only** owned paths (CODEOWNERS enforced when available).
5. Link ticket / LLD section.
6. CI must pass: backend tests, frontend build/lint, Flyway validate (when configured).

### Merge rules

- Squash merge preferred for feature branches (clean `develop` history).
- Require ≥1 review (Lead for foundation & shared; peer + Lead for features touching `cases` or ports).
- No merge with failing required checks.
- `main` ← `develop` only via release PR.

---

## 3. Code review checklist

**Architecture**

- [ ] No feature→feature dependency
- [ ] Uses shared ports for Case/Workflow/Notification/Lookup
- [ ] Follows module template

**API / DB**

- [ ] Path & DTO match OpenAPI
- [ ] Flyway file ownership + unique version
- [ ] No edit of applied migrations
- [ ] Indexes on new FKs

**Code quality**

- [ ] Constructor injection; no field `@Autowired`
- [ ] No bare RuntimeException / System.out
- [ ] Validation on requests; `@PreAuthorize` present
- [ ] Tests: happy + negative

**Frontend**

- [ ] Feature folder only
- [ ] React Query + Zod as applicable
- [ ] Loading/error/empty/loaded
- [ ] data-testid contract

---

## 4. Working in parallel without conflicts

| Technique | Detail |
|-----------|--------|
| Path ownership | Each dev stays in `features/<own>` + own Flyway files + own UI feature |
| Shared file discipline | Changes to `routes/index`, OpenAPI components, `cases` popup config → small PRs owned by Lead or sequenced |
| Frequent rebase/merge from `develop` | Daily pull to reduce drift |
| Timestamp Flyway versions | Avoids version number fights |
| Interface-first | Features compile against ports while Lead swaps stubs |
| Don’t reformat unrelated files | Prevents noise conflicts |
| CODEOWNERS | Auto-request reviewers per path |

### High-conflict files (serialize changes)

- `CasesMtApplication.java` (rare)
- `pom.xml` / `package.json` (Lead coordinates dependency adds)
- `application*.yml` (Lead; features request keys)
- Shared Cases List / Case Type popup
- `db/migration/common/*`

---

## 5. Hotfix flow

```
main → hotfix/<ticket> → PR to main → tag → back-merge to develop
```

Lead-only unless delegated.
