# Local scripts

Helpers for local development. Do not commit secrets (`.env` is git-ignored).

## Start the backend

**One-time:** `copy cases-MT\.env.example cases-MT\.env` and set `DB_URL`, `DB_USER`, `DB_PASSWORD`.

From `cases-MT` (`.env` is loaded automatically at startup):

```powershell
.\mvnw.cmd -DskipTests spring-boot:run
```

Optional helper from repo root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\local\run-backend.ps1
```

### Production / CI

Inject `DB_URL`, `DB_USER`, and `DB_PASSWORD` as real environment variables (or a secret manager). Those override any local `.env`.
