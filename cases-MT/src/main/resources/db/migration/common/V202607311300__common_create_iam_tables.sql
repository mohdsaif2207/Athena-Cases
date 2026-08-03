-- IAM foundation for dynamic User Management / RBAC (Login module)
-- Ready for Admin UI to manage users, roles, permissions, case types, and teams.
-- Does NOT seed business users (DBM / ExRT / Billing).

CREATE TABLE roles (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(500),
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    version         INTEGER      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(64)  NOT NULL,
    updated_by      VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_roles_code UNIQUE (code)
);

CREATE TABLE permissions (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(120) NOT NULL,
    description     VARCHAR(500),
    version         INTEGER      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(64)  NOT NULL,
    updated_by      VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_permissions_code UNIQUE (code)
);

CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(64)  NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    display_name    VARCHAR(120) NOT NULL,
    email           VARCHAR(255),
    status          VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    version         INTEGER      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(64)  NOT NULL,
    updated_by      VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_status_valid CHECK (status IN ('ACTIVE', 'INACTIVE', 'LOCKED'))
);

CREATE INDEX ix_users_status ON users (status);

CREATE TABLE user_roles (
    user_id         BIGINT       NOT NULL,
    role_id         BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE INDEX ix_user_roles_role_id ON user_roles (role_id);

CREATE TABLE role_permissions (
    role_id         BIGINT       NOT NULL,
    permission_id   BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_role_permissions PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission_id FOREIGN KEY (permission_id) REFERENCES permissions (id)
);

CREATE INDEX ix_role_permissions_permission_id ON role_permissions (permission_id);

CREATE TABLE teams (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(120) NOT NULL,
    team_type       VARCHAR(32)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    version         INTEGER      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(64)  NOT NULL,
    updated_by      VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_teams_code UNIQUE (code),
    CONSTRAINT chk_teams_team_type_valid CHECK (team_type IN ('INITIATING', 'RECEIVING', 'BOTH'))
);

CREATE INDEX ix_teams_active ON teams (active);

CREATE TABLE case_types (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(120) NOT NULL,
    module_key      VARCHAR(64)  NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    version         INTEGER      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(64)  NOT NULL,
    updated_by      VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_case_types_code UNIQUE (code)
);

CREATE INDEX ix_case_types_active ON case_types (active);

-- Foundation permission catalog (assignable via Admin UI later)
INSERT INTO permissions (code, name, description, created_by, updated_by) VALUES
    ('CASES_ACCESS', 'Cases Access', 'Open Cases menu and dashboard', 'SYSTEM', 'SYSTEM'),
    ('CASES_VIEW', 'Cases View', 'View case details', 'SYSTEM', 'SYSTEM'),
    ('CASES_CREATE', 'Cases Create', 'Create new cases', 'SYSTEM', 'SYSTEM'),
    ('CASES_EDIT', 'Cases Edit', 'Edit existing cases', 'SYSTEM', 'SYSTEM'),
    ('CASES_EXPORT', 'Cases Export', 'Export cases grid to Excel', 'SYSTEM', 'SYSTEM'),
    ('WF_VIEW', 'Workflow View', 'View workflow grid', 'SYSTEM', 'SYSTEM'),
    ('NOTIF_VIEW', 'Notification View', 'View notification grid', 'SYSTEM', 'SYSTEM');

INSERT INTO roles (code, name, description, created_by, updated_by) VALUES
    ('SYSTEM_ADMINISTRATOR', 'System Administrator', 'Full platform administrator', 'SYSTEM', 'SYSTEM');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SYSTEM_ADMINISTRATOR';

-- Case type catalog only (not business users / teams)
INSERT INTO case_types (code, name, module_key, created_by, updated_by) VALUES
    ('BILLING_DEPARTMENT_REQUEST', 'Billing Department Request', 'BILLING', 'SYSTEM', 'SYSTEM'),
    ('DBM_WORK_ORDER_REQUEST', 'DBM Work Order Request', 'DBM', 'SYSTEM', 'SYSTEM'),
    ('EXRT_REQUEST', 'ExRT Request', 'EXRT', 'SYSTEM', 'SYSTEM'),
    ('COVERAGE_AMOUNT_REQUEST', 'Coverage Amount Request', 'COVERAGE', 'SYSTEM', 'SYSTEM'),
    ('REPORT_REQUEST', 'Report Request', 'REPORT', 'SYSTEM', 'SYSTEM'),
    ('PROJECT_TRACKER_REQUEST', 'Project Tracker Request', 'PROJECT', 'SYSTEM', 'SYSTEM');
