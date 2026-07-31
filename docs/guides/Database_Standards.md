# Database Standards (Guide)

Canonical rules: [docs/architecture/07_Database_Strategy.md](../architecture/07_Database_Strategy.md)

## Quick rules

1. Flyway only — scripts under `cases-MT/src/main/resources/db/migration/`
2. Timestamp version: `VyyyyMMddHHmm__{owner}_{desc}.sql`
3. Header comment with `owner:`
4. Never edit applied migrations
5. Feature tables FK → `cases.id`; no back-FK from `cases`
6. Audit columns on business tables; `TIMESTAMPTZ`; explicit `VARCHAR(n)`
