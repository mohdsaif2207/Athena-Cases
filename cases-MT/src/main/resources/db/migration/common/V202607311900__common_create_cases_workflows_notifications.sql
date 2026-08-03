-- Cases spine + workflow/notification queues (Step 1: list/read only; auto-create on save is Step 2).

CREATE TABLE cases (
    id                   BIGSERIAL PRIMARY KEY,
    case_number          VARCHAR(64)  NOT NULL,
    case_type_id         BIGINT       NOT NULL,
    client_id            VARCHAR(64),
    subject              VARCHAR(200) NOT NULL,
    description          VARCHAR(2000),
    case_owner           VARCHAR(120) NOT NULL,
    case_status          VARCHAR(64)  NOT NULL,
    priority             VARCHAR(32)  NOT NULL DEFAULT 'Medium',
    carrier              VARCHAR(120),
    assigned_to          VARCHAR(120),
    segment_id           VARCHAR(64),
    frequency            VARCHAR(64),
    spoken_key           VARCHAR(120),
    event_id             VARCHAR(64),
    mail_month           VARCHAR(7),
    requested_due_date   DATE,
    version              INTEGER      NOT NULL DEFAULT 1,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by           VARCHAR(64)  NOT NULL,
    updated_by           VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_cases_case_number UNIQUE (case_number),
    CONSTRAINT fk_cases_case_type_id FOREIGN KEY (case_type_id) REFERENCES case_types (id),
    CONSTRAINT chk_cases_priority_valid CHECK (priority IN ('High', 'Medium', 'Low'))
);

CREATE INDEX ix_cases_case_type_id ON cases (case_type_id);
CREATE INDEX ix_cases_case_status ON cases (case_status);
CREATE INDEX ix_cases_created_at ON cases (created_at DESC);

CREATE TABLE workflows (
    id                   BIGSERIAL PRIMARY KEY,
    case_id              BIGINT       NOT NULL,
    receiving_team_id    BIGINT       NOT NULL,
    message_key          VARCHAR(120) NOT NULL,
    message_id           VARCHAR(120) NOT NULL,
    message_name         VARCHAR(255) NOT NULL,
    message_object       VARCHAR(255),
    status               VARCHAR(64)  NOT NULL,
    decision             VARCHAR(120),
    owner_name           VARCHAR(120),
    priority             VARCHAR(32)  NOT NULL DEFAULT 'Medium',
    received_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    action_label         VARCHAR(64),
    logs                 VARCHAR(4000),
    version              INTEGER      NOT NULL DEFAULT 1,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by           VARCHAR(64)  NOT NULL,
    updated_by           VARCHAR(64)  NOT NULL,
    CONSTRAINT fk_workflows_case_id FOREIGN KEY (case_id) REFERENCES cases (id),
    CONSTRAINT fk_workflows_receiving_team_id FOREIGN KEY (receiving_team_id) REFERENCES teams (id)
);

CREATE INDEX ix_workflows_case_id ON workflows (case_id);
CREATE INDEX ix_workflows_receiving_team_id ON workflows (receiving_team_id);
CREATE INDEX ix_workflows_received_at ON workflows (received_at DESC);
CREATE INDEX ix_workflows_status ON workflows (status);

CREATE TABLE notifications (
    id                   BIGSERIAL PRIMARY KEY,
    case_id              BIGINT       NOT NULL,
    receiving_team_id    BIGINT       NOT NULL,
    message_key          VARCHAR(120) NOT NULL,
    message_id           VARCHAR(120) NOT NULL,
    message_name         VARCHAR(255) NOT NULL,
    message_object       VARCHAR(255),
    message              VARCHAR(2000) NOT NULL,
    received_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    details              VARCHAR(4000),
    version              INTEGER      NOT NULL DEFAULT 1,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    created_by           VARCHAR(64)  NOT NULL,
    updated_by           VARCHAR(64)  NOT NULL,
    CONSTRAINT fk_notifications_case_id FOREIGN KEY (case_id) REFERENCES cases (id),
    CONSTRAINT fk_notifications_receiving_team_id FOREIGN KEY (receiving_team_id) REFERENCES teams (id)
);

CREATE INDEX ix_notifications_case_id ON notifications (case_id);
CREATE INDEX ix_notifications_receiving_team_id ON notifications (receiving_team_id);
CREATE INDEX ix_notifications_received_at ON notifications (received_at DESC);

-- Seed sample cases (for Step 1 grid display) linked to seeded case types / teams
INSERT INTO cases (
    case_number, case_type_id, client_id, subject, description, case_owner, case_status,
    priority, carrier, assigned_to, segment_id, frequency, spoken_key, event_id, mail_month,
    requested_due_date, created_by, updated_by
)
SELECT
    'CASE-1001',
    ct.id,
    'CLT-001',
    'Sample Billing Request',
    'Seeded billing case for dashboard verification',
    'ADMKL',
    'Requested',
    'Medium',
    'Carrier A',
    'Billing Ops',
    'SEG-100',
    'Once',
    'SK-1',
    'EVT-1',
    '07-2026',
    DATE '2026-08-15',
    'SYSTEM',
    'SYSTEM'
FROM case_types ct
WHERE ct.code = 'BILLING_DEPARTMENT_REQUEST';

INSERT INTO cases (
    case_number, case_type_id, client_id, subject, description, case_owner, case_status,
    priority, carrier, assigned_to, segment_id, frequency, spoken_key, event_id, mail_month,
    requested_due_date, created_by, updated_by
)
SELECT
    'CASE-1002',
    ct.id,
    'CLT-002',
    'Sample DBM Work Order',
    'Seeded DBM case for dashboard verification',
    'ADMKL',
    'In Progress',
    'High',
    'Carrier B',
    'DBM',
    'SEG-200',
    'Weekly',
    'SK-2',
    'EVT-2',
    '08-2026',
    DATE '2026-08-20',
    'SYSTEM',
    'SYSTEM'
FROM case_types ct
WHERE ct.code = 'DBM_WORK_ORDER_REQUEST';

INSERT INTO workflows (
    case_id, receiving_team_id, message_key, message_id, message_name, message_object,
    status, decision, owner_name, priority, received_at, action_label, logs, created_by, updated_by
)
SELECT
    c.id,
    t.id,
    'WF-BILLING-001',
    'MSG-9001',
    'Billing Department Request Workflow',
    'CASE-1001',
    'Pending Assignment',
    NULL,
    'Billing Ops Queue',
    'Medium',
    NOW(),
    'Assign',
    'Seeded workflow awaiting receiving-team assignment',
    'SYSTEM',
    'SYSTEM'
FROM cases c
CROSS JOIN teams t
WHERE c.case_number = 'CASE-1001' AND t.code = 'BILLING_OPS_TEAM';

INSERT INTO workflows (
    case_id, receiving_team_id, message_key, message_id, message_name, message_object,
    status, decision, owner_name, priority, received_at, action_label, logs, created_by, updated_by
)
SELECT
    c.id,
    t.id,
    'WF-DBM-001',
    'MSG-9002',
    'DBM Work Order Workflow',
    'CASE-1002',
    'Pending Assignment',
    NULL,
    'DBM Queue',
    'High',
    NOW(),
    'Assign',
    'Seeded DBM workflow awaiting assignment',
    'SYSTEM',
    'SYSTEM'
FROM cases c
CROSS JOIN teams t
WHERE c.case_number = 'CASE-1002' AND t.code = 'DBM_TEAM';

INSERT INTO notifications (
    case_id, receiving_team_id, message_key, message_id, message_name, message_object,
    message, received_at, details, created_by, updated_by
)
SELECT
    c.id,
    t.id,
    'NTF-BILLING-001',
    'MSG-9001',
    'Billing Assignment Notice',
    'CASE-1001',
    'A new Billing Department Request (Case #CASE-1001) has been assigned to your team.',
    NOW(),
    'Deep link: /cases?caseNumber=CASE-1001',
    'SYSTEM',
    'SYSTEM'
FROM cases c
CROSS JOIN teams t
WHERE c.case_number = 'CASE-1001' AND t.code = 'BILLING_OPS_TEAM';

INSERT INTO notifications (
    case_id, receiving_team_id, message_key, message_id, message_name, message_object,
    message, received_at, details, created_by, updated_by
)
SELECT
    c.id,
    t.id,
    'NTF-DBM-001',
    'MSG-9002',
    'DBM Assignment Notice',
    'CASE-1002',
    'A new DBM Work Order Request (Case #CASE-1002) has been assigned to your team.',
    NOW(),
    'Deep link: /cases?caseNumber=CASE-1002',
    'SYSTEM',
    'SYSTEM'
FROM cases c
CROSS JOIN teams t
WHERE c.case_number = 'CASE-1002' AND t.code = 'DBM_TEAM';
