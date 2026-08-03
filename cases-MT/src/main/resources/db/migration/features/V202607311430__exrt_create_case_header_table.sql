-- owner: exrt
-- author: shaik.umar
-- feature: ExRT Request
-- note: Shared case header required for ExRT create. Lead may later align naming to `cases`; keep column contract stable.

CREATE TABLE IF NOT EXISTS case_header (
    id                  BIGSERIAL       PRIMARY KEY,
    case_number         VARCHAR(64)     NOT NULL,
    case_type           VARCHAR(64)     NOT NULL,
    client_id           VARCHAR(64),
    subject             VARCHAR(200),
    description         VARCHAR(1000),
    case_owner          VARCHAR(128)    NOT NULL,
    status              VARCHAR(64)     NOT NULL,
    priority            VARCHAR(32)     NOT NULL,
    requested_due_date  DATE,
    workflow_triggered  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by          VARCHAR(64)     NOT NULL,
    updated_by          VARCHAR(64)     NOT NULL,
    version             INTEGER         NOT NULL DEFAULT 1,
    CONSTRAINT uq_case_header_case_number UNIQUE (case_number),
    CONSTRAINT chk_case_header_priority CHECK (priority IN ('HIGH', 'MEDIUM', 'LOW'))
);
