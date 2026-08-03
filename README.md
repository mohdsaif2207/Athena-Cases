# Athena Cases

Greenfield **Case Management System** monorepo — foundation-first, multi-case-type, parallel team ownership.

## Start here

1. [Architecture Overview](docs/architecture/00_Architecture_Overview.md) — binding blueprint  
2. [Developer Onboarding](docs/guides/Developer_Onboarding_Guide.md)  
3. [Development Roadmap](docs/architecture/11_Development_Roadmap.md) — Lead executes Phases 1–5 before feature unlock  

## Branching

- Work from `develop`.
- Do **not** commit feature work directly to `main`.
- Do **not** merge into `main` unless the team explicitly agrees (release PR).
- Full rules: [Git Workflow](docs/guides/Git_Workflow.md)

## Repository structure

```
Athena-Cases/
├── cases-MT/      # Backend (Spring Boot / Java 21)        ← alias: backend/
├── cases-UI/      # Frontend (React + Vite + TypeScript) ← alias: frontend/
├── api/           # OpenAPI contracts
├── database/      # Seeds + DB docs (Flyway runs from cases-MT resources)
├── docs/          # Architecture, guides, ADRs
├── scripts/       # Local + CI helpers
├── .github/       # PR template, CODEOWNERS, workflows
├── .gitignore
└── README.md
```

Folder responsibilities: [01_Monorepo_Structure.md](docs/architecture/01_Monorepo_Structure.md)

## Team ownership

| Owner | Scope |
|-------|--------|
| Dev 1 (Lead) | Foundation, shared ports, security, common Flyway, docs/CI |
| Dev 2 | Billing Department Request |
| Dev 3 | DBM Work Order Request |
| Dev 4 | ExRT Request |

**Hard rule:** No feature module depends on another feature module.

## Prerequisites

| Tool | Version |
|------|---------|
| JDK | 21+ |
| Node.js | 20+ (LTS) |
| PostgreSQL | 14+ (when DB phase lands) |

Maven Wrapper is included under `cases-MT` (`mvnw` / `mvnw.cmd`).

## Backend (`cases-MT`)

```powershell
# Local (loads cases-MT/.env, then starts Spring Boot)
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\local\run-backend.ps1
```

One-time: `copy cases-MT\.env.example cases-MT\.env` and set `DB_URL`, `DB_USER`, `DB_PASSWORD`.

```bash
cd cases-MT
.\mvnw.cmd clean verify
```

Shared service **interfaces** (no feature implementations) live under:

`casemanagement`, `workflow`, `notification`, `lookup`, `permission`, `filestorage`, `audit`, `security`

## Frontend (`cases-UI`)

```bash
cd cases-UI
npm install
npm run dev
npm run build
```

Feature folders: `src/features/<case-type>/` — see [Frontend Guide](docs/guides/Frontend_Development_Guide.md)

## Documentation index

| Guide | Path |
|-------|------|
| Architecture | `docs/guides/Architecture_Guide.md` |
| Backend | `docs/guides/Backend_Development_Guide.md` |
| Frontend | `docs/guides/Frontend_Development_Guide.md` |
| API | `docs/guides/API_Standards.md` |
| Database | `docs/guides/Database_Standards.md` |
| Git | `docs/guides/Git_Workflow.md` |
| Coding | `docs/guides/Coding_Standards.md` |
| Onboarding | `docs/guides/Developer_Onboarding_Guide.md` |

## Status

Foundation blueprint + port interfaces + package scaffolding are in place.  
**Business features (Billing / DBM / ExRT) are intentionally not implemented** — wait for Lead **Foundation Ready** signal (Roadmap Phase 5 exit).
