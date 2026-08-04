-- DBM LLD: persist pending_dbm_approval on shared cases when Transfer Type = Custom.
-- Idempotent add — column may already exist if applied manually.

ALTER TABLE cases
    ADD COLUMN IF NOT EXISTS pending_dbm_approval BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN cases.pending_dbm_approval IS
    'True when DBM Work Order Transfer Type requires DBM manager approval (Custom).';
