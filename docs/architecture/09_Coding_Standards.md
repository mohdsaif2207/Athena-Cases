# 09 — Coding Standards

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)

---

## 1. Naming conventions

| Kind | Convention |
|------|------------|
| Packages | `com.athena.cases...` lowercase |
| Classes | `PascalCase` |
| Methods / fields | `camelCase` |
| Constants | `UPPER_SNAKE` |
| DB | `snake_case` |
| REST paths | `kebab-case` |
| React components | `PascalCase.tsx` |
| React hooks | `useX` |
| Test methods | `should_<expected>_when_<condition>` |

---

## 2. Package conventions

- One feature = one package under `features`.
- No `util` dumping ground for feature logic — keep utils pure and generic in `common.util`.
- Avoid circular packages; ArchUnit enforces feature isolation.

---

## 3. Dependency injection

- **Constructor injection only** (`@RequiredArgsConstructor` or explicit ctor).
- No field `@Autowired`.
- Inject interfaces (ports), not feature concretes from other modules.

---

## 4. Logging

- Use `@Slf4j` (Java) / structured logger.
- Levels: INFO business events; WARN handled anomalies; ERROR failures; DEBUG diagnostics.
- Always include contextual fields: `requestId`, `userId`, `caseId`, `caseType`.
- Never log passwords, tokens, full payloads with PII.

---

## 5. Exceptions

- No bare `throw new RuntimeException("...")`.
- Use domain exceptions handled by `GlobalExceptionHandler`.
- Catch specific exceptions; log + wrap/rethrow as typed errors.
- Do not swallow exceptions.

---

## 6. Validation

- Request DTOs: Bean Validation annotations.
- Cross-field: `@AssertTrue` or custom class-level validators.
- Service-layer checks for authorization and state transitions.

---

## 7. Transactions

- `@Transactional` on service write methods (public).
- Never on controllers or private methods expecting proxy behavior.
- `readOnly = true` for queries.
- Multi-step writes in one service transaction unless after-commit event ADR.

---

## 8. Mapper rules

- MapStruct preferred for >3 fields.
- Mappers are stateless Spring components.
- Do not put business rules in mappers.

---

## 9. Repository rules

- Spring Data JPA interfaces only in `repository` packages.
- No native SQL in features without Lead review.
- Prefer `Optional` + `Page`/`Slice`; avoid unbounded `findAll()` on growing tables.
- FK indexes created in same migration as FK.

---

## 10. Controller rules

- Thin: validate → call service → map response.
- `@PreAuthorize` on every endpoint (or explicit public marker).
- No business branching beyond HTTP concerns.
- OpenAPI annotations / kept in sync with `api/openapi`.

---

## 11. DTO rules

- Separate Request / Response records.
- Never expose JPA entities.
- Use JSON property names `camelCase`.
- Include `version` on update requests for optimistic locking.

---

## 12. Frontend coding standards (summary)

- No `any`; strict TypeScript.
- React Query for server state; RHF+Zod for forms.
- No feature→feature imports.
- `data-testid` on actionable elements.
- No secrets in Vite env — public keys/URLs only (`VITE_*`).

---

## 13. Banned patterns (hard refuse)

See ADC-style refusals adapted to this repo: field injection, `System.out.println`, hardcoded secrets/URLs, feature peer deps, CSV multi-select on new tables, editing applied Flyway scripts.
