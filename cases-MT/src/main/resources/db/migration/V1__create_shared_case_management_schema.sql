    -- V1: Shared case-management foundation (LLD)
    -- Tables: cases, workflows, notifications
    -- DBM-specific detail table is intentionally deferred to a later migration.

    -- =============================================================================
    -- cases — shared header for every case type (Cases grid / Case Details)
    -- =============================================================================
    -- id          = internal database primary key (surrogate key; used by FKs).
    -- case_number = business Case ID shown to users (unique).
    --               For DBM Work Order Request, the service layer will generate:
    --               DBM000001, DBM000002, DBM000003, ...
    --               (Generation logic is not defined in this migration.)
    -- =============================================================================
    CREATE TABLE cases (
        id                      BIGSERIAL       PRIMARY KEY,  -- internal PK only
        case_number             VARCHAR(32)     NOT NULL,     -- business Case ID (e.g. DBM000001)
        case_type               VARCHAR(64)     NOT NULL,
        subject                 VARCHAR(200)    NOT NULL,
        description             VARCHAR(1000)   NULL,
        status                  VARCHAR(64)     NOT NULL,
        priority                VARCHAR(16)     NOT NULL,
        case_owner              VARCHAR(120)    NOT NULL,
        requested_due_date      DATE            NOT NULL,
        client_id               VARCHAR(64)     NULL,
        pending_dbm_approval    BOOLEAN         NOT NULL DEFAULT FALSE,
        created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
        updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
        created_by              VARCHAR(64)     NOT NULL,
        updated_by              VARCHAR(64)     NOT NULL,
        CONSTRAINT uq_cases_case_number UNIQUE (case_number)
    );

    CREATE INDEX ix_cases_case_type ON cases (case_type);
    CREATE INDEX ix_cases_status ON cases (status);
    CREATE INDEX ix_cases_case_owner ON cases (case_owner);
    CREATE INDEX ix_cases_requested_due_date ON cases (requested_due_date);

    -- =============================================================================
    -- workflows — one simplified workflow instance per case (queue / status on Cases screen)
    -- =============================================================================
    CREATE TABLE workflows (
        id                      BIGSERIAL       PRIMARY KEY,
        case_id                 BIGINT          NOT NULL,  -- FK → cases.id (internal PK, not case_number)
        workflow_type           VARCHAR(64)     NOT NULL,
        status                  VARCHAR(64)     NOT NULL,
        assigned_team           VARCHAR(64)     NOT NULL,
        created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
        updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
        created_by              VARCHAR(64)     NOT NULL,
        updated_by              VARCHAR(64)     NOT NULL,
        CONSTRAINT uq_workflows_case_id UNIQUE (case_id),
        CONSTRAINT fk_workflows_case_id FOREIGN KEY (case_id)
            REFERENCES cases (id)
    );

    CREATE INDEX ix_workflows_assigned_team ON workflows (assigned_team);
    CREATE INDEX ix_workflows_status ON workflows (status);

    -- =============================================================================
    -- notifications — in-app alerts on Cases screen (filtered by recipient team)
    -- =============================================================================
    CREATE TABLE notifications (
        id                      BIGSERIAL       PRIMARY KEY,
        case_id                 BIGINT          NOT NULL,  -- FK → cases.id (internal PK, not case_number)
        recipient_team          VARCHAR(64)     NOT NULL,
        message                 VARCHAR(500)    NOT NULL,
        is_read                 BOOLEAN         NOT NULL DEFAULT FALSE,
        created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
        updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
        created_by              VARCHAR(64)     NOT NULL,
        updated_by              VARCHAR(64)     NOT NULL,
        CONSTRAINT fk_notifications_case_id FOREIGN KEY (case_id)
            REFERENCES cases (id)
    );

    CREATE INDEX ix_notifications_case_id ON notifications (case_id);
    CREATE INDEX ix_notifications_recipient_team ON notifications (recipient_team);
    CREATE INDEX ix_notifications_recipient_team_is_read ON notifications (recipient_team, is_read);
