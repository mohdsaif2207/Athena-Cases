-- Align billing_department_requests.client_id with shared client codes (VARCHAR).
-- Shared schema cases_mt already has VARCHAR; some envs may still have BIGINT from
-- V202608031700. Idempotent: only alters when the column is still bigint.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'billing_department_requests'
          AND column_name = 'client_id'
          AND data_type = 'bigint'
    ) THEN
        ALTER TABLE billing_department_requests
            ALTER COLUMN client_id TYPE VARCHAR(64)
            USING CASE
                WHEN client_id IS NULL THEN NULL
                ELSE client_id::text
            END;
    END IF;
END $$;
