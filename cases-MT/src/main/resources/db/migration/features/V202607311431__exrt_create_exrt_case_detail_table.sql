-- owner: exrt
-- author: shaik.umar
-- feature: ExRT Request

CREATE TABLE IF NOT EXISTS exrt_case_detail (
    id                          BIGSERIAL       PRIMARY KEY,
    case_id                     BIGINT          NOT NULL,
    first_name                  VARCHAR(120)    NOT NULL,
    last_name                   VARCHAR(120)    NOT NULL,
    mi                          VARCHAR(1),
    state                       VARCHAR(64),
    phone_number                VARCHAR(10),
    product_id                  VARCHAR(64)     NOT NULL,
    coverage_id                 VARCHAR(64),
    carrier_id                  VARCHAR(64)     NOT NULL,
    customer_contact_email      VARCHAR(255),
    tier_ii_agent_id            VARCHAR(64),
    exrt_type                   VARCHAR(32)     NOT NULL,
    policy_number               VARCHAR(25),
    disposition                 VARCHAR(128)    NOT NULL,
    inquiry_source              VARCHAR(64)     NOT NULL,
    action_needed               VARCHAR(64)     NOT NULL,
    request_assigned_to         VARCHAR(128),
    reason_for_escalation       VARCHAR(128)    NOT NULL,
    reason_code_1               VARCHAR(128)    NOT NULL,
    requestor_notes             VARCHAR(1000),
    notes_issues                VARCHAR(1000),
    coaching_feedback           VARCHAR(1000),
    call_center_education       BOOLEAN         NOT NULL DEFAULT FALSE,
    case_origin                 VARCHAR(64)     NOT NULL,
    web_mail                    VARCHAR(255),
    contact_name                VARCHAR(200),
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    created_by                  VARCHAR(64)     NOT NULL,
    updated_by                  VARCHAR(64)     NOT NULL,
    version                     INTEGER         NOT NULL DEFAULT 1,
    CONSTRAINT uq_exrt_case_detail_case_id UNIQUE (case_id),
    CONSTRAINT fk_exrt_case_detail_case_id
        FOREIGN KEY (case_id) REFERENCES case_header (id)
);
