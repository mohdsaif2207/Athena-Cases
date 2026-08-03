-- owner: exrt
-- author: shaik.umar
-- feature: ExRT Request

CREATE INDEX IF NOT EXISTS ix_case_header_case_type ON case_header (case_type);
CREATE INDEX IF NOT EXISTS ix_case_header_status ON case_header (status);
CREATE INDEX IF NOT EXISTS ix_case_header_case_owner ON case_header (case_owner);
CREATE INDEX IF NOT EXISTS ix_case_header_created_at ON case_header (created_at DESC);

CREATE INDEX IF NOT EXISTS ix_exrt_case_detail_carrier_id ON exrt_case_detail (carrier_id);
CREATE INDEX IF NOT EXISTS ix_exrt_case_detail_product_id ON exrt_case_detail (product_id);
CREATE INDEX IF NOT EXISTS ix_exrt_case_detail_exrt_type ON exrt_case_detail (exrt_type);

ALTER TABLE case_header
    DROP CONSTRAINT IF EXISTS chk_case_header_exrt_status;

ALTER TABLE case_header
    ADD CONSTRAINT chk_case_header_case_type_known
    CHECK (case_type IN (
        'EXRT_REQUEST',
        'BILLING_DEPARTMENT_REQUEST',
        'DBM_WORK_ORDER_REQUEST',
        'COVERAGE_AMOUNT_REQUEST',
        'REPORT_REQUEST',
        'PROJECT_TRACKER_REQUEST'
    ));
