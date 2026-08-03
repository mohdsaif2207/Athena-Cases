# ExRT feature (Dev 4)

Owns ExRT Request **create** API, Flyway feature migrations, and integration with shared ports:

- `CurrentUserService` — Case Owner
- `WorkflowService` — start once on create (DBM / PENDING_ASSIGNMENT)
- `NotificationService` — notify DBM team
- `LookupService` — clients / products

UI: `cases-UI/src/features/exrt-request`

API:

- `POST /api/v1/cases/exrt-requests`
- `GET /api/v1/cases/exrt-requests/{caseId}`
- `GET /api/v1/lookups/exrt/**`
