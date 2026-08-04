-- owner: billing
-- Align client_id with shared lookup codes (CLIENT001…) and ExRT/DBM String clientId.
-- Must run after shared head ~202608041620 (teammate migrations may have left BIGINT).

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'billing_department_requests'
          AND column_name = 'client_id'
          AND (data_type = 'bigint' OR udt_name = 'int8')
    ) THEN
        ALTER TABLE billing_department_requests
            ALTER COLUMN client_id TYPE VARCHAR(64)
            USING CASE
                WHEN client_id IS NULL THEN NULL
                ELSE client_id::text
            END;
    END IF;
END $$;
