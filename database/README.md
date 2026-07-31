# Database artifacts

## Authoritative migrations

Flyway scripts that the application runs live in:

```
cases-MT/src/main/resources/db/migration/
  common/
  audit/
  lookups/
  features/
```

## This folder

| Path | Purpose |
|------|---------|
| `flyway/` | Optional mirrored SQL for human review / DBA handoff |
| `seeds/` | Non-prod seed scripts (never auto-run in prod/pci) |

See: `docs/architecture/07_Database_Strategy.md`
