# Frontend Development Guide

**App:** `cases-UI`

## Prerequisites

- Node.js 20+ LTS
- npm

## Run locally

```bash
cd cases-UI
npm install
npm run dev
```

Point `VITE_API_BASE_URL` at the backend (see `.env.example` when added).

## Feature work

```
src/features/<your-case-type>/
```

Export routes from `routes.tsx`; register via Lead-owned router composition if needed.

## Stack rules

| Concern | Use |
|---------|-----|
| Server data | TanStack Query |
| HTTP | `src/api` Axios client |
| Forms | React Hook Form + Zod |
| UI | MUI (+ shared wrappers in `components/ui`) |

## Do not

- Import another feature folder
- Put secrets in `VITE_*`
- Fetch in components without React Query
- Skip loading/error/empty states

Standards: [03_Frontend_Architecture.md](../architecture/03_Frontend_Architecture.md) · [09_Coding_Standards.md](../architecture/09_Coding_Standards.md)
