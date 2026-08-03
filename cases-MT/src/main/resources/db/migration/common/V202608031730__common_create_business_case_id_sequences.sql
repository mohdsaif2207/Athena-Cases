-- owner: common / Lead
-- Shared business-case display ID allocation (BIL######, DBM######, …).
-- Feature modules call BusinessCaseIdService; they must not own sequences.

CREATE TABLE business_case_id_sequences (
    case_type_code  VARCHAR(64)  NOT NULL,
    prefix          VARCHAR(8)   NOT NULL,
    width           INTEGER      NOT NULL DEFAULT 6,
    next_value      BIGINT       NOT NULL DEFAULT 1,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_business_case_id_sequences PRIMARY KEY (case_type_code),
    CONSTRAINT chk_business_case_id_sequences_width
        CHECK (width >= 1 AND width <= 12),
    CONSTRAINT chk_business_case_id_sequences_next_value
        CHECK (next_value >= 1),
    CONSTRAINT chk_business_case_id_sequences_prefix
        CHECK (prefix ~ '^[A-Z]{2,8}$')
);

INSERT INTO business_case_id_sequences (case_type_code, prefix, width, next_value) VALUES
    ('BILLING_DEPARTMENT_REQUEST', 'BIL', 6, 1),
    ('DBM_WORK_ORDER_REQUEST', 'DBM', 6, 1),
    ('EXRT_REQUEST', 'EXR', 6, 1);
