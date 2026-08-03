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

   Cases module users (seeded when `app.default.admin.seed-enabled=true`):
   | Username | Password             | Display | Case type (New Case) | Teams | Home queues |
   |----------|----------------------|---------|----------------------|-------|-------------|
   | charan   | FranklinCharan@123   | MTcharan | Billing only | Billing initiating + receiving scope | Hidden (no WF/NOTIF perm) |
   | saif     | FranklinSaif@123     | MTsaif | DBM only | Client Success, Client Analyst + DBM receiving | Hidden (no WF/NOTIF; grant later in Utilities) |
   | umar     | FranklinUmar@123     | MTumar | ExRT only | ExRT initiating + receiving | Shown (receiving + WF/NOTIF) |

Successful login redirects to the Home screen.

Optional: copy `.env.example` to `.env.development` and set `VITE_API_BASE_URL`.  
If unset, the Vite dev proxy forwards `/api` to `http://localhost:8080`.
