-- owner: billing
-- author: billing.dev
-- feature: Billing Department Request
-- lld: Billing_Department_Request_LLD.md §13.2
--
-- RE-VERSIONED: originally V202607311910 — never applied on shared Postgres
-- (Flyway head already past that version). New ID is after current shared head
-- V202608031600 so migrate runs in order without outOfOrder.
--
-- DEPENDENCY (shared — not owned by Billing):
--   Requires table "cases" (Lead / Case Management Flyway) to exist before this
--   migration runs in any integrated environment. Billing stores case_id (BIGINT)
--   as FK → cases.id ON DELETE CASCADE. Do not create or alter "cases" here.
--
-- BUSINESS CASE ID (approved Phase-1 decision #7 — not in canonical LLD §13.2):
--   Column business_case_id stores the Billing display identifier (format BIL######).
--   Not the surrogate PK. Generation strategy is PENDING shared Case Management /
--   platform ID generation integration — do NOT introduce a Billing-owned sequence
--   without architectural approval. Shared display identity on cases is case_number.
--   TODO Replace with actual implementation after module integration
--   (shared ID generation / CaseManagementService numbering strategy).

CREATE TABLE billing_department_requests (
    id                              BIGSERIAL       NOT NULL,
    case_id                         BIGINT          NOT NULL,
    business_case_id                VARCHAR(16)     NOT NULL,
    request_type                    VARCHAR(64)     NULL,
    client_id                       BIGINT          NULL,
    campaign_id                     VARCHAR(64)     NULL,
    reason_for_importance           VARCHAR(500)    NULL,
    daily_issue_report              BOOLEAN         NOT NULL DEFAULT FALSE,
    approx_number_of_coverages      INTEGER         NULL,
    approx_revenue_impact           NUMERIC(18, 2)  NULL,
    requested_due_date              DATE            NULL,
    effective_date                  DATE            NULL,
    segment_id                      BIGINT          NULL,
    product_id                      BIGINT          NULL,
    anticipated_release_date        DATE            NULL,
    request_description             VARCHAR(5000)   NOT NULL,
    billing_extract_type            VARCHAR(32)     NULL,
    pre_note_request_type           VARCHAR(32)     NULL,
    billing_institution             VARCHAR(255)    NULL,
    target_post_date                DATE            NULL,
    bill_set                        VARCHAR(255)    NULL,
    billing_cycle                   VARCHAR(255)    NULL,
    prior_hard_declines             VARCHAR(8)      NULL,
    hard_decline_codes              VARCHAR(500)    NULL,
    billing_hold_type               VARCHAR(32)     NULL,
    hold_reason                     VARCHAR(1000)   NULL,
    billing_hold_by_product_id      BIGINT          NULL,
    created_at                      TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT NOW(),
    created_by                      VARCHAR(64)     NOT NULL,
    updated_by                      VARCHAR(64)     NOT NULL,
    version                         INTEGER         NOT NULL DEFAULT 1,

    CONSTRAINT pk_billing_department_requests
        PRIMARY KEY (id),

    CONSTRAINT uq_billing_department_requests_case_id
        UNIQUE (case_id),

    CONSTRAINT uq_billing_department_requests_business_case_id
        UNIQUE (business_case_id),

    CONSTRAINT fk_billing_department_requests_case_id
        FOREIGN KEY (case_id) REFERENCES cases (id) ON DELETE CASCADE,

    CONSTRAINT chk_billing_department_requests_request_type
        CHECK (request_type IS NULL OR request_type IN (
            'Compliance/Legal',
            'Extract',
            'Operations Alert',
            'Reject Review',
            'Research',
            'Schedule'
        )),

    CONSTRAINT chk_billing_department_requests_billing_extract_type
        CHECK (billing_extract_type IS NULL OR billing_extract_type IN (
            'Billing',
            'Pre Note',
            'Rebill'
        )),

    CONSTRAINT chk_billing_department_requests_pre_note_request_type
        CHECK (pre_note_request_type IS NULL OR pre_note_request_type IN (
            'All',
            'Changes Only'
        )),

    CONSTRAINT chk_billing_department_requests_prior_hard_declines
        CHECK (prior_hard_declines IS NULL OR prior_hard_declines IN (
            'Yes',
            'No'
        )),

    CONSTRAINT chk_billing_department_requests_billing_hold_type
        CHECK (billing_hold_type IS NULL OR billing_hold_type IN (
            'Client Level',
            'Coverage Level',
            'Product Level',
            'Segment Level'
        )),

    CONSTRAINT chk_billing_department_requests_business_case_id
        CHECK (business_case_id ~ '^BIL[0-9]{6}$')
);

CREATE INDEX ix_billing_department_requests_client_id
    ON billing_department_requests (client_id);

CREATE INDEX ix_billing_department_requests_campaign_id
    ON billing_department_requests (campaign_id);

CREATE INDEX ix_billing_department_requests_segment_id
    ON billing_department_requests (segment_id);

CREATE INDEX ix_billing_department_requests_product_id
    ON billing_department_requests (product_id);
