# 05 — Shared Services (Ports Only)

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)  
**Rule:** Interfaces + responsibilities only in this blueprint. Implementations are Lead-owned stubs first, then real adapters.

Java interfaces live under:

`cases-MT/src/main/java/com/athena/cases/{casemanagement|workflow|notification|lookup|permission|filestorage|audit|security}/`

---

## 1. CaseManagementService

**Responsibility:** Own the shared case header lifecycle used by every case type.

| Method (conceptual) | Responsibility |
|---------------------|----------------|
| `createCase(CreateCaseCommand)` | Persist `cases` row; generate `case_number`; set owner/priority/status defaults; return `CaseRef` |
| `getCase(caseId)` | Load header or throw not found |
| `updateCaseHeader(caseId, UpdateCaseHeaderCommand)` | Update only shared fields; enforce optimistic lock via `version` |
| `linkParentCase(caseId, parentCaseId)` | Optional parent linkage with validation |

**Must NOT:** Know Billing/DBM/ExRT columns.  
**Used by:** Every feature service on create/update.

---

## 2. WorkflowService

**Responsibility:** Start and query workflow instances linked to a case.

| Method (conceptual) | Responsibility |
|---------------------|----------------|
| `start(StartWorkflowCommand)` | Create workflow for `caseId` with receiver team + initial status |
| `getByCaseId(caseId)` | Read current workflow state |
| `transition(...)` | Future — out of early Billing create scope |

**Must NOT:** Persist case-type fields.  
**Transaction note:** Prefer after successful case+extension persist; failure policy documented in ADR (same TX vs after-commit).

---

## 3. NotificationService

**Responsibility:** Notify users/teams of case events.

| Method (conceptual) | Responsibility |
|---------------------|----------------|
| `notifyTeam(NotifyTeamCommand)` | Persist/send team notification with message + deep link |
| `notifyUser(NotifyUserCommand)` | User-targeted notification |

**Must NOT:** Call third-party SMS/email SDKs from feature modules — only via this port’s adapter.

---

## 4. LookupService

**Responsibility:** Read-only reference data for dropdowns/search.

| Method (conceptual) | Responsibility |
|---------------------|----------------|
| `searchClients(query)` | Client name search |
| `listActiveCampaigns()` | Active campaign IDs |
| `listSegments(clientId)` | Segments for client |
| `listProducts(...)` | Product / PCP lists |
| `findParentCases(query)` | Parent case search |

**Must NOT:** Own master data schema inside feature modules.  
**Stub:** Return seed JSON until platform APIs exist.

---

## 5. CurrentUserService

**Responsibility:** Resolve authenticated principal for services (not HTTP layer).

| Method (conceptual) | Responsibility |
|---------------------|----------------|
| `requireUserId()` | Current user id/username or throw unauthorized |
| `requireDisplayName()` | For Case Owner defaulting |
| `hasPermission(code)` | Convenience RBAC check |

---

## 6. PermissionService

**Responsibility:** Central permission evaluation (roles → permissions).

| Method (conceptual) | Responsibility |
|---------------------|----------------|
| `check(userId, permission)` | Boolean |
| `require(userId, permission)` | Throw `ForbiddenException` if missing |
| `listPermissions(userId)` | For UI gating |

---

## 7. FileStorageService

**Responsibility:** Store/retrieve attachments (future case types).

| Method (conceptual) | Responsibility |
|---------------------|----------------|
| `store(StoreFileCommand)` | Returns storage key |
| `load(storageKey)` | Stream/bytes |
| `delete(storageKey)` | Soft/hard delete per policy |

**Must NOT:** Expose filesystem paths to API clients.

---

## 8. AuditService

**Responsibility:** Record security-sensitive / business-significant actions.

| Method (conceptual) | Responsibility |
|---------------------|----------------|
| `record(AuditEvent)` | Persist actor, action, entity type/id, timestamp, correlation id |

Features may call for domain events; JPA auditing covers row-level timestamps separately.

---

## 9. Interaction diagram (create case)

```
FeatureService
   │
   ├─► PermissionService.require(CASES_CREATE)
   ├─► CurrentUserService.requireDisplayName()   // Case Owner
   ├─► CaseManagementService.createCase(...)
   ├─► FeatureRepository.save(extension)
   ├─► WorkflowService.start(...)
   └─► NotificationService.notifyTeam(...)
```

---

## 10. Stub policy

Until real adapters exist:

- Stubs are `@Profile("dev")` or always-on `@Primary` with clear `// STUB` class names (`StubWorkflowService`).
- Stubs must be **deterministic** for tests.
- Features write integration tests against stubs; Lead replaces stubs without feature code changes.
