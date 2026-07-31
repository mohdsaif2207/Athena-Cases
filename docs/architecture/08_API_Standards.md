# 08 — API Standards

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)

---

## 1. URL conventions

| Rule | Example |
|------|---------|
| Prefix | `/api/v{N}/...` |
| Current version | `/api/v1/` |
| Resources | plural kebab-case | `/api/v1/billing-department-requests` |
| IDs | path variables = internal `cases.id` unless documented otherwise |
| Nested lookups under feature | `/api/v1/billing-department-requests/lookups/hold-levels` |
| Shared lookups | `/api/v1/lookups/clients` (Lead / Lookup module) |
| No verbs in paths | Prefer HTTP method + resource |

**Case number** is display-only in messages; REST paths use numeric `caseId`.

---

## 2. Versioning

- URI versioning (`v1`) for breaking changes.
- Additive non-breaking fields allowed in place.
- Deprecate with `Deprecation` headers + docs; remove only in next major.

---

## 3. HTTP methods & status codes

| Method | Use | Success |
|--------|-----|---------|
| `GET` | Read | `200` |
| `POST` | Create | `201` + `Location` when applicable |
| `PUT` | Full/replace update of editable resource (as designed) | `200` |
| `PATCH` | Partial update (if introduced later) | `200` |
| `DELETE` | Soft-delete preferred | `204` or `200` |

Errors: `400` validation, `401` unauthenticated, `403` forbidden, `404` not found, `409` conflict (version / duplicate), `422` optional for semantic validation, `500` unexpected.

---

## 4. Success response format

**Recommended envelope:**

```json
{
  "data": { },
  "requestId": "req_...",
  "timestamp": "2026-07-31T06:00:00Z"
}
```

List:

```json
{
  "data": [ ],
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "sort": "createdAt,desc"
  },
  "requestId": "req_...",
  "timestamp": "2026-07-31T06:00:00Z"
}
```

*(ADR may allow bare DTO for create responses if OpenAPI documents it; stay consistent project-wide.)*

---

## 5. Error format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request Description is required",
    "field": "requestDescription",
    "requestId": "req_...",
    "timestamp": "2026-07-31T06:00:00Z",
    "details": [
      { "field": "priority", "message": "must be one of High, Medium, Low", "code": "INVALID_ENUM" }
    ]
  }
}
```

---

## 6. Pagination / sorting / filtering

| Concern | Convention |
|---------|------------|
| Page | `page` zero-based query param (default `0`) |
| Size | `size` (default `20`, max `100`) |
| Sort | `sort=field,asc|desc` (whitelist fields per endpoint) |
| Filter | Explicit query params (`status`, `caseType`, `q`) — no free-form SQL |

---

## 7. Validation

- Bean Validation on request DTOs (`@Valid`).
- Field errors mapped into `error.details`.
- Server is source of truth; UI Zod mirrors rules but is not security.

---

## 8. Security

- All `/api/v1/**` authenticated unless explicitly public (health only).
- `@PreAuthorize` per endpoint.
- Never return secrets; mask PII in logs.

---

## 9. Swagger / OpenAPI standards

- Single source: `api/openapi/openapi-v1.yaml` (export from springdoc optional — keep file in sync).
- Each operation: `operationId`, summary, tags = feature module name.
- Document error responses with shared schema components.
- Examples under `api/examples/`.

**Tags:** `Cases`, `BillingDepartmentRequest`, `DbmWorkOrderRequest`, `ExrtRequest`, `Lookups`, `Admin`.

---

## 10. Idempotency & concurrency

- Updates carry `version` (from `cases.version`); mismatch → `409`.
- Creates are not idempotent by default unless `Idempotency-Key` ADR added later.
