# Backend Development Guide

**Module:** `cases-MT`  
**Package root:** `com.athena.cases`

## Prerequisites

- JDK 21+
- Docker (optional, for Postgres)
- Maven Wrapper (`./mvnw` / `.\mvnw.cmd`)

## Run locally

```bash
cd cases-MT
.\mvnw.cmd spring-boot:run
```

Set `SPRING_PROFILES_ACTIVE=dev` (default once configured).

## Where to put code

| If you are… | Work only under |
|-------------|-----------------|
| Lead | `common`, `config`, `security`, `audit`, ports packages, `db/migration/common|audit|lookups` |
| Billing | `features/billing/**`, `db/migration/features/*billing*` |
| DBM | `features/dbm/**`, matching migrations |
| ExRT | `features/exrt/**`, matching migrations |

## Adding an endpoint (feature)

1. DTO records in `features/<m>/dto`
2. MapStruct mapper
3. Service calling ports + repository
4. Controller with `@PreAuthorize` + `@Valid`
5. Tests (service + WebMvcTest)
6. Update `api/openapi/openapi-v1.yaml`

## Do not

- Depend on another feature package
- Return JPA entities
- Commit secrets in `application-*.yml`
- Edit shared migrations without Lead

Full standards: [09_Coding_Standards.md](../architecture/09_Coding_Standards.md) · [08_API_Standards.md](../architecture/08_API_Standards.md)
