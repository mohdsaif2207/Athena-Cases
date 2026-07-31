# Athena Cases

Greenfield case-management monorepo for collaborative development.

## Branching

- Work from the `develop` branch.
- Do **not** commit feature work directly to `main`.
- Do **not** merge into `main` unless the team explicitly agrees.

## Repository structure

```
Athena-Cases/
├── cases-MT/      # Spring Boot backend (Java 21, Maven)
├── cases-UI/      # React frontend (Vite + TypeScript)
├── database/      # SQL scripts
├── docs/          # LLD and project documentation
├── .gitignore
└── README.md
```

## Prerequisites

| Tool | Version |
| ---- | ------- |
| JDK | 21+ |
| Node.js | 20+ (LTS recommended) |
| npm | bundled with Node |

Maven is not required globally — `cases-MT` includes the Maven Wrapper (`mvnw` / `mvnw.cmd`).

## Backend (`cases-MT`)

```bash
cd cases-MT
./mvnw spring-boot:run          # macOS / Linux
.\mvnw.cmd spring-boot:run      # Windows

./mvnw clean verify             # build + tests
```

Default port: `8080` (Spring Boot default).

## Frontend (`cases-UI`)

```bash
cd cases-UI
npm install
npm run dev      # local development server
npm run build    # production build
```

## Database & docs

- Place SQL scripts under `database/`.
- Place LLD and other design docs under `docs/`.

## Notes

This repository is initialized as a clean scaffold only — no business features, APIs, or schema yet.
