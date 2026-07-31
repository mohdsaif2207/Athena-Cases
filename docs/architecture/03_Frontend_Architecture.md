# 03 — React Architecture

**Parent:** [00_Architecture_Overview.md](./00_Architecture_Overview.md)  
**App:** `cases-UI` (Vite + React + TypeScript + MUI)

---

## 1. Folder structure

```
cases-UI/src/
├── app/                    # Providers composition (QueryClient, Router, Theme, Auth)
├── layouts/                # AppLayout, AuthLayout — chrome only
├── pages/                  # Login, Forbidden, NotFound (non-feature)
├── components/
│   ├── ui/                 # Buttons, inputs wrapping MUI (shared design language)
│   ├── forms/              # Shared form primitives
│   └── feedback/           # Toast, ConfirmDialog, Empty/Error/Loading states
├── hooks/                  # useDebounce, useCurrentUser (shared)
├── api/                    # axios instance, interceptors, error normalizer
├── services/               # Optional domain-agnostic helpers (prefer feature api/)
├── contexts/               # AuthContext, optional AppConfigContext
├── routes/                 # route tree; lazy-load feature routes
├── store/                  # UI-only state (sidebar open, etc.) — NOT server cache
├── theme/                  # MUI createTheme + tokens
├── assets/images|icons/
├── types/                  # Shared TS types (ApiError, Page, User)
├── utils/                  # formatDate, assertNever, etc.
├── constants/              # route paths, permission keys
├── validation/             # shared Zod refinements
└── features/
    ├── _template/          # Copy-me feature layout
    ├── cases/              # Cases List + Case Type selection (shared shell)
    ├── billing-department-request/
    ├── dbm-work-order-request/
    └── exrt-request/
```

---

## 2. Why each folder exists

| Folder | Why it exists |
|--------|----------------|
| `app/` | Single place to wire providers — avoids `main.tsx` sprawl |
| `layouts/` | Keep navigation chrome out of feature pages |
| `pages/` | Cross-cutting routes not owned by a case type |
| `components/` | Reusable UI; **no feature API calls** |
| `hooks/` | Shared behavior without duplicating React Query keys wrongly |
| `api/` | One Axios client; auth header + 401 handling |
| `contexts/` | Auth/session for guards; keep narrow |
| `routes/` | Central registration; feature exports `routes.tsx` |
| `store/` | Rare local global UI state — **server data stays in React Query** |
| `theme/` | MUI tokens — no hard-coded hex in features |
| `types/` / `utils/` / `constants/` / `validation/` | Shared contracts & helpers |
| `features/*` | **Ownership boundary** — parallel work with minimal merge conflicts |

---

## 3. Feature module layout (frontend)

```
features/<case-type>/
├── pages/          # Route-level screens (Create / View / Edit)
├── components/     # Feature-only sections
├── hooks/          # useQuery / useMutation wrappers
├── api/            # REST calls for this feature only
├── types/          # Feature DTO types (align with OpenAPI)
├── validation/     # Zod schemas
├── routes.tsx      # Export routes for central router
└── index.ts        # Public barrel (pages/routes only)
```

**Rules:**

- Features import shared `api/client`, `components/*`, `theme`, `types`.
- Features **must not** import another `features/<peer>/*`.
- Navigate to other case types only via shared `features/cases` routes / case id links.

---

## 4. Data & form stack

| Concern | Library | Rule |
|---------|---------|------|
| Server state | TanStack Query | `queryKey` namespaced: `['billing', caseId]` |
| HTTP | Axios via `api/client` | No raw `fetch` in components |
| Forms | React Hook Form + Zod | Schema colocated in feature `validation/` |
| UI | MUI | Wrap in shared `components/ui` when reused twice+ |
| Routing | React Router | Lazy routes per feature |

Mandatory UI states for data-bound views: **loading / error / empty / loaded**.

---

## 5. Conflict minimization (frontend)

| Practice | Effect |
|----------|--------|
| One folder per developer feature | Rare overlapping diffs |
| Shared shell owned by Lead / Cases Search | Feature PRs don’t fight over Cases List |
| Route registration = small file edits | Use `routes/index.ts` importing feature `routes.tsx` — keep feature-owned files for path strings when possible |
| No shared “god” form component | Each case type owns its form |
