-- V2: DBM Work Order Request — feature-specific schema
-- Does NOT alter V1 shared tables (cases, workflows, notifications).
-- Shared header fields (subject, priority, status, case_owner, requested_due_date,
-- description, client_id, pending_dbm_approval) remain on cases only.

-- =============================================================================
-- dbm_work_order — 1:1 DBM detail row for a case
-- Stores only DBM form fields not already on cases.
-- =============================================================================
CREATE TABLE dbm_work_order (
    case_id                         BIGINT          NOT NULL,
    -- Section 1: Request Information (DBM-only)
    vendor                          VARCHAR(32)     NOT NULL,
    core_processor_conversion       BOOLEAN         NOT NULL DEFAULT FALSE,
    -- Section 2: File Request
    transfer_type                   VARCHAR(64)     NOT NULL,
    return_file_expected            VARCHAR(8)      NOT NULL,
    pgp_key_at_acxiom               VARCHAR(8)      NULL,
    expected_quantity               INTEGER         NULL,
    frequency                       VARCHAR(32)     NULL,
    special_instructions            TEXT            NULL,
    -- Section 3: Marketing Research Request (client_id lives on cases)
    event_id                        VARCHAR(64)     NULL,
    media_ids                       TEXT            NULL,
    mail_month                      VARCHAR(32)     NULL,
    media_out_quantity              INTEGER         NULL,
    changes_to_matchback_db         BOOLEAN         NOT NULL DEFAULT FALSE,
    selection_criteria              TEXT            NULL,
    matchback_field                 VARCHAR(200)    NULL,
    change_to                       VARCHAR(200)    NULL,
    -- Section 4: For DBM Use Only
    dbm_work_order_number           VARCHAR(64)     NULL,
    dbm_completion_notes            TEXT            NULL,
    total_records_updated           INTEGER         NULL,
    -- Audit
    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by                      VARCHAR(64)     NOT NULL,
    updated_by                      VARCHAR(64)     NOT NULL,
    CONSTRAINT pk_dbm_work_order PRIMARY KEY (case_id),
    CONSTRAINT fk_dbm_work_order_case_id FOREIGN KEY (case_id)
        REFERENCES cases (id)
);

-- =============================================================================
-- dbm_work_order_coverage_levels — multi-select Coverage Level (no CSV/JSON)
-- Values e.g. Complementary, Voluntary, Cancels
-- =============================================================================
CREATE TABLE dbm_work_order_coverage_levels (
    id                              BIGSERIAL       PRIMARY KEY,
    case_id                         BIGINT          NOT NULL,
    coverage_level                  VARCHAR(64)     NOT NULL,
    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by                      VARCHAR(64)     NOT NULL,
    updated_by                      VARCHAR(64)     NOT NULL,
    CONSTRAINT uq_dbm_coverage_levels_case_value UNIQUE (case_id, coverage_level),
    CONSTRAINT fk_dbm_coverage_levels_case_id FOREIGN KEY (case_id)
        REFERENCES dbm_work_order (case_id)
);

CREATE INDEX ix_dbm_coverage_levels_case_id ON dbm_work_order_coverage_levels (case_id);

-- =============================================================================
-- dbm_work_order_account_types — multi-select Requested Account Types (no CSV/JSON)
-- Values e.g. Share/ESHAR, Share Draft/ES/DR, Checking/ECHK, Savings/ESAV
-- =============================================================================
CREATE TABLE dbm_work_order_account_types (
    id                              BIGSERIAL       PRIMARY KEY,
    case_id                         BIGINT          NOT NULL,
    account_type                    VARCHAR(64)     NOT NULL,
    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by                      VARCHAR(64)     NOT NULL,
    updated_by                      VARCHAR(64)     NOT NULL,
    CONSTRAINT uq_dbm_account_types_case_value UNIQUE (case_id, account_type),
    CONSTRAINT fk_dbm_account_types_case_id FOREIGN KEY (case_id)
        REFERENCES dbm_work_order (case_id)
);

CREATE INDEX ix_dbm_account_types_case_id ON dbm_work_order_account_types (case_id);

-- =============================================================================
-- dbm_work_order_spoken_keys — multi-select Spoken Keys (no CSV/JSON)
-- Lookup codes/ids selected on the Marketing Research section
-- =============================================================================
CREATE TABLE dbm_work_order_spoken_keys (
    id                              BIGSERIAL       PRIMARY KEY,
    case_id                         BIGINT          NOT NULL,
    spoken_key                      VARCHAR(64)     NOT NULL,
    created_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by                      VARCHAR(64)     NOT NULL,
    updated_by                      VARCHAR(64)     NOT NULL,
    CONSTRAINT uq_dbm_spoken_keys_case_value UNIQUE (case_id, spoken_key),
    CONSTRAINT fk_dbm_spoken_keys_case_id FOREIGN KEY (case_id)
        REFERENCES dbm_work_order (case_id)
);

CREATE INDEX ix_dbm_spoken_keys_case_id ON dbm_work_order_spoken_keys (case_id);
