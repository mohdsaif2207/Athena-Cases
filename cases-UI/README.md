# Athena Cases UI

React + TypeScript + Vite frontend for Athena Cases.

## Login module (current)

1. Start backend (`cases-MT`) with the `dev` profile (default).
2. Install and run the UI:

```bash
npm install
npm run dev
```

3. Open http://localhost:5173/login  
   Default admin (from `application-dev.yml`):
   - Username: `fm_admin`
   - Password: `FM@Admin123`

Successful login redirects to the Cases Dashboard placeholder.

Optional: copy `.env.example` to `.env.development` and set `VITE_API_BASE_URL`.  
If unset, the Vite dev proxy forwards `/api` to `http://localhost:8080`.
