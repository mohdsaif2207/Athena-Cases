-- RBAC foundation extension: groups, team membership, role scopes, case-type team mappings.
-- Does not modify prior changeset; additive only.

CREATE TABLE groups (
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
    CONSTRAINT uq_groups_code UNIQUE (code)
);

CREATE TABLE user_groups (
    user_id         BIGINT       NOT NULL,
    group_id        BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_user_groups PRIMARY KEY (user_id, group_id),
    CONSTRAINT fk_user_groups_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_groups_group_id FOREIGN KEY (group_id) REFERENCES groups (id)
);

CREATE INDEX ix_user_groups_group_id ON user_groups (group_id);

CREATE TABLE group_roles (
    group_id        BIGINT       NOT NULL,
    role_id         BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_group_roles PRIMARY KEY (group_id, role_id),
    CONSTRAINT fk_group_roles_group_id FOREIGN KEY (group_id) REFERENCES groups (id),
    CONSTRAINT fk_group_roles_role_id FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE INDEX ix_group_roles_role_id ON group_roles (role_id);

CREATE TABLE user_teams (
    user_id         BIGINT       NOT NULL,
    team_id         BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_user_teams PRIMARY KEY (user_id, team_id),
    CONSTRAINT fk_user_teams_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_teams_team_id FOREIGN KEY (team_id) REFERENCES teams (id)
);

CREATE INDEX ix_user_teams_team_id ON user_teams (team_id);

CREATE TABLE role_case_types (
    role_id         BIGINT       NOT NULL,
    case_type_id    BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_role_case_types PRIMARY KEY (role_id, case_type_id),
    CONSTRAINT fk_role_case_types_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_case_types_case_type_id FOREIGN KEY (case_type_id) REFERENCES case_types (id)
);

CREATE INDEX ix_role_case_types_case_type_id ON role_case_types (case_type_id);

CREATE TABLE role_initiating_teams (
    role_id         BIGINT       NOT NULL,
    team_id         BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_role_initiating_teams PRIMARY KEY (role_id, team_id),
    CONSTRAINT fk_role_initiating_teams_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_initiating_teams_team_id FOREIGN KEY (team_id) REFERENCES teams (id)
);

CREATE INDEX ix_role_initiating_teams_team_id ON role_initiating_teams (team_id);

CREATE TABLE role_receiving_teams (
    role_id         BIGINT       NOT NULL,
    team_id         BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_role_receiving_teams PRIMARY KEY (role_id, team_id),
    CONSTRAINT fk_role_receiving_teams_role_id FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_receiving_teams_team_id FOREIGN KEY (team_id) REFERENCES teams (id)
);

CREATE INDEX ix_role_receiving_teams_team_id ON role_receiving_teams (team_id);

CREATE TABLE case_type_initiating_teams (
    case_type_id    BIGINT       NOT NULL,
    team_id         BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_case_type_initiating_teams PRIMARY KEY (case_type_id, team_id),
    CONSTRAINT fk_ct_init_teams_case_type_id FOREIGN KEY (case_type_id) REFERENCES case_types (id),
    CONSTRAINT fk_ct_init_teams_team_id FOREIGN KEY (team_id) REFERENCES teams (id)
);

CREATE INDEX ix_case_type_initiating_teams_team_id ON case_type_initiating_teams (team_id);

CREATE TABLE case_type_receiving_teams (
    case_type_id    BIGINT       NOT NULL,
    team_id         BIGINT       NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_case_type_receiving_teams PRIMARY KEY (case_type_id, team_id),
    CONSTRAINT fk_ct_recv_teams_case_type_id FOREIGN KEY (case_type_id) REFERENCES case_types (id),
    CONSTRAINT fk_ct_recv_teams_team_id FOREIGN KEY (team_id) REFERENCES teams (id)
);

CREATE INDEX ix_case_type_receiving_teams_team_id ON case_type_receiving_teams (team_id);

-- Additional permission for Admin Dashboard (assigned via DB, not hardcoded in business logic)
INSERT INTO permissions (code, name, description, created_by, updated_by)
SELECT 'ADMIN_ACCESS', 'Admin Access', 'Open Admin Dashboard and manage IAM master data', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE code = 'ADMIN_ACCESS');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SYSTEM_ADMINISTRATOR'
  AND p.code = 'ADMIN_ACCESS'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Default teams (master data for Admin Dashboard)
INSERT INTO teams (code, name, team_type, created_by, updated_by) VALUES
    ('CLIENT_SERVICES', 'Client Services', 'INITIATING', 'SYSTEM', 'SYSTEM'),
    ('LEGAL_TEAM', 'Legal Team', 'INITIATING', 'SYSTEM', 'SYSTEM'),
    ('EXECUTIVE_RESPONSE_TEAM', 'Executive Response Team', 'BOTH', 'SYSTEM', 'SYSTEM'),
    ('BILLING_OPS_TEAM', 'Billing Operations', 'RECEIVING', 'SYSTEM', 'SYSTEM'),
    ('CLIENT_SUCCESS', 'Client Success', 'INITIATING', 'SYSTEM', 'SYSTEM'),
    ('CLIENT_ANALYST', 'Client Analyst', 'INITIATING', 'SYSTEM', 'SYSTEM'),
    ('DBM_TEAM', 'DBM', 'RECEIVING', 'SYSTEM', 'SYSTEM');

-- Case type ↔ initiating / receiving team mappings (business master data)
INSERT INTO case_type_initiating_teams (case_type_id, team_id)
SELECT ct.id, t.id FROM case_types ct CROSS JOIN teams t
WHERE ct.code = 'DBM_WORK_ORDER_REQUEST' AND t.code IN ('CLIENT_SUCCESS', 'CLIENT_ANALYST');

INSERT INTO case_type_receiving_teams (case_type_id, team_id)
SELECT ct.id, t.id FROM case_types ct CROSS JOIN teams t
WHERE ct.code = 'DBM_WORK_ORDER_REQUEST' AND t.code = 'DBM_TEAM';

INSERT INTO case_type_initiating_teams (case_type_id, team_id)
SELECT ct.id, t.id FROM case_types ct CROSS JOIN teams t
WHERE ct.code = 'EXRT_REQUEST' AND t.code IN ('CLIENT_SERVICES', 'LEGAL_TEAM');

INSERT INTO case_type_receiving_teams (case_type_id, team_id)
SELECT ct.id, t.id FROM case_types ct CROSS JOIN teams t
WHERE ct.code = 'EXRT_REQUEST' AND t.code = 'EXECUTIVE_RESPONSE_TEAM';

INSERT INTO case_type_initiating_teams (case_type_id, team_id)
SELECT ct.id, t.id FROM case_types ct CROSS JOIN teams t
WHERE ct.code = 'BILLING_DEPARTMENT_REQUEST' AND t.code IN ('CLIENT_SERVICES', 'EXECUTIVE_RESPONSE_TEAM');

INSERT INTO case_type_receiving_teams (case_type_id, team_id)
SELECT ct.id, t.id FROM case_types ct CROSS JOIN teams t
WHERE ct.code = 'BILLING_DEPARTMENT_REQUEST' AND t.code = 'BILLING_OPS_TEAM';

-- Default group for System Administrator (User → Group → Role)
INSERT INTO groups (code, name, description, created_by, updated_by) VALUES
    ('SYSTEM_ADMINISTRATORS', 'System Administrators', 'Full platform administrators', 'SYSTEM', 'SYSTEM');

INSERT INTO group_roles (group_id, role_id)
SELECT g.id, r.id
FROM groups g
CROSS JOIN roles r
WHERE g.code = 'SYSTEM_ADMINISTRATORS' AND r.code = 'SYSTEM_ADMINISTRATOR';

-- SYSTEM_ADMINISTRATOR role scopes: all case types + all teams
INSERT INTO role_case_types (role_id, case_type_id)
SELECT r.id, ct.id
FROM roles r
CROSS JOIN case_types ct
WHERE r.code = 'SYSTEM_ADMINISTRATOR';

INSERT INTO role_initiating_teams (role_id, team_id)
SELECT r.id, t.id
FROM roles r
CROSS JOIN teams t
WHERE r.code = 'SYSTEM_ADMINISTRATOR'
  AND t.team_type IN ('INITIATING', 'BOTH');

INSERT INTO role_receiving_teams (role_id, team_id)
SELECT r.id, t.id
FROM roles r
CROSS JOIN teams t
WHERE r.code = 'SYSTEM_ADMINISTRATOR'
  AND t.team_type IN ('RECEIVING', 'BOTH');
