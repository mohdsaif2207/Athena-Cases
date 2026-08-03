-- owner: billing
-- author: billing.dev
-- feature: Billing Department Request
-- lld: Billing_Department_Request_LLD.md §13.3
--
-- RE-VERSIONED: originally V202607311911 — never applied on shared Postgres
-- (Flyway head already past that version). New ID is after
-- V202608031700 (billing_department_requests).
--
-- DEPENDENCY:
--   Requires billing_department_requests (Billing migration V202608031700).
--
-- Hold Level Available options may later vary by Billing Hold Type (OPEN-01).
-- Stored codes remain within the CHECK domain below (interim flat set).

CREATE TABLE billing_request_hold_levels (
    id                      BIGSERIAL       NOT NULL,
    billing_request_id      BIGINT          NOT NULL,
    hold_level_code         VARCHAR(32)     NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_billing_request_hold_levels
        PRIMARY KEY (id),

    CONSTRAINT uq_billing_request_hold_levels_req_code
        UNIQUE (billing_request_id, hold_level_code),

    CONSTRAINT fk_billing_request_hold_levels_billing_request_id
        FOREIGN KEY (billing_request_id)
            REFERENCES billing_department_requests (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_billing_request_hold_levels_hold_level_code
        CHECK (hold_level_code IN (
            'All',
            'Auto Cancel',
            'Billing',
            'Pre Note',
            'Rebill',
            'Refund'
        ))
);

CREATE INDEX ix_billing_request_hold_levels_billing_request_id
    ON billing_request_hold_levels (billing_request_id);
