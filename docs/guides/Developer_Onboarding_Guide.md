# Developer Onboarding Guide

Welcome to **Athena Cases**.

## Day 0 — Read

1. [Architecture Overview](../architecture/00_Architecture_Overview.md) (assumptions + ownership)
2. Your lane:
   - Lead → Roadmap Phases 1–5
   - Feature → [Feature Module Template](../architecture/06_Feature_Module_Template.md)
3. [Git Workflow](./Git_Workflow.md)

## Day 1 — Environment

| Tool | Version |
|------|---------|
| JDK | 21+ |
| Node | 20+ LTS |
| Postgres | 14+ (local or Docker) |
| IDE | IntelliJ / VS Code + Cursor |

```bash
# Backend
cd cases-MT && .\mvnw.cmd verify

# Frontend
cd cases-UI && npm install && npm run build
```

## Day 2 — Mental model

```
UI feature  →  REST /api/v1/...  →  Feature Service  →  Ports (Case/WF/Notify/Lookup) + Feature Repo
                                                      →  PostgreSQL
```

You never call another case-type module.

## Day 3 — First contribution

1. Branch from `develop`
2. Touch **only** owned folders
3. Open PR with template checklist
4. Request Lead review if shared contracts change

## Who to ask

| Topic | Owner |
|-------|-------|
| Ports, security, Flyway common | Dev 1 (Lead) |
| Billing | Dev 2 |
| DBM | Dev 3 |
| ExRT | Dev 4 |

## Definition of ready for feature coding

Lead announces **Foundation Ready** per [04_Shared_Foundation.md](../architecture/04_Shared_Foundation.md) §4.
