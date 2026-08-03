-- Per-user case module roles: Charan=Billing, Saif=DBM, Umar=ExRT.
-- Queues (WF/NOTIF) are granted only on roles that include receiving-team scopes.

-- ---- Billing (charan) — initiating only; create/view Billing; no WF/NOTIF ----
INSERT INTO roles (code, name, description, created_by, updated_by)
SELECT 'BILLING_CASE_USER', 'Billing Case User', 'Billing Department Request initiator', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'BILLING_CASE_USER');

INSERT INTO groups (code, name, description, created_by, updated_by)
SELECT 'BILLING_CASE_USERS', 'Billing Case Users', 'Users for Billing Department Request', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM groups WHERE code = 'BILLING_CASE_USERS');

INSERT INTO group_roles (group_id, role_id)
SELECT g.id, r.id FROM groups g CROSS JOIN roles r
WHERE g.code = 'BILLING_CASE_USERS' AND r.code = 'BILLING_CASE_USER'
  AND NOT EXISTS (SELECT 1 FROM group_roles gr WHERE gr.group_id = g.id AND gr.role_id = r.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'BILLING_CASE_USER' AND p.code IN ('CASES_VIEW', 'CASES_CREATE')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO role_case_types (role_id, case_type_id)
SELECT r.id, ct.id FROM roles r CROSS JOIN case_types ct
WHERE r.code = 'BILLING_CASE_USER' AND ct.code = 'BILLING_DEPARTMENT_REQUEST'
  AND NOT EXISTS (SELECT 1 FROM role_case_types x WHERE x.role_id = r.id AND x.case_type_id = ct.id);

INSERT INTO role_initiating_teams (role_id, team_id)
SELECT r.id, t.id FROM roles r CROSS JOIN teams t
WHERE r.code = 'BILLING_CASE_USER' AND t.code IN ('CLIENT_SERVICES', 'EXECUTIVE_RESPONSE_TEAM')
  AND NOT EXISTS (SELECT 1 FROM role_initiating_teams x WHERE x.role_id = r.id AND x.team_id = t.id);

-- Billing receiving team on role for scope metadata; no WF_VIEW/NOTIF_VIEW → queues stay hidden for initiator
INSERT INTO role_receiving_teams (role_id, team_id)
SELECT r.id, t.id FROM roles r CROSS JOIN teams t
WHERE r.code = 'BILLING_CASE_USER' AND t.code = 'BILLING_OPS_TEAM'
  AND NOT EXISTS (SELECT 1 FROM role_receiving_teams x WHERE x.role_id = r.id AND x.team_id = t.id);

-- ---- DBM (saif) — initiating + receiving DBM; create/view DBM; WF/NOTIF for receiving ----
INSERT INTO roles (code, name, description, created_by, updated_by)
SELECT 'DBM_CASE_USER', 'DBM Case User', 'DBM Work Order Request user', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'DBM_CASE_USER');

INSERT INTO groups (code, name, description, created_by, updated_by)
SELECT 'DBM_CASE_USERS', 'DBM Case Users', 'Users for DBM Work Order Request', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM groups WHERE code = 'DBM_CASE_USERS');

INSERT INTO group_roles (group_id, role_id)
SELECT g.id, r.id FROM groups g CROSS JOIN roles r
WHERE g.code = 'DBM_CASE_USERS' AND r.code = 'DBM_CASE_USER'
  AND NOT EXISTS (SELECT 1 FROM group_roles gr WHERE gr.group_id = g.id AND gr.role_id = r.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'DBM_CASE_USER'
  AND p.code IN ('CASES_VIEW', 'CASES_CREATE', 'WF_VIEW', 'NOTIF_VIEW')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO role_case_types (role_id, case_type_id)
SELECT r.id, ct.id FROM roles r CROSS JOIN case_types ct
WHERE r.code = 'DBM_CASE_USER' AND ct.code = 'DBM_WORK_ORDER_REQUEST'
  AND NOT EXISTS (SELECT 1 FROM role_case_types x WHERE x.role_id = r.id AND x.case_type_id = ct.id);

INSERT INTO role_initiating_teams (role_id, team_id)
SELECT r.id, t.id FROM roles r CROSS JOIN teams t
WHERE r.code = 'DBM_CASE_USER' AND t.code IN ('CLIENT_SUCCESS', 'CLIENT_ANALYST')
  AND NOT EXISTS (SELECT 1 FROM role_initiating_teams x WHERE x.role_id = r.id AND x.team_id = t.id);

INSERT INTO role_receiving_teams (role_id, team_id)
SELECT r.id, t.id FROM roles r CROSS JOIN teams t
WHERE r.code = 'DBM_CASE_USER' AND t.code = 'DBM_TEAM'
  AND NOT EXISTS (SELECT 1 FROM role_receiving_teams x WHERE x.role_id = r.id AND x.team_id = t.id);

-- ---- ExRT (umar) — both initiating + receiving; create/view ExRT; WF/NOTIF ----
INSERT INTO roles (code, name, description, created_by, updated_by)
SELECT 'EXRT_CASE_USER', 'ExRT Case User', 'Executive Response Team (ExRT) Request user', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'EXRT_CASE_USER');

INSERT INTO groups (code, name, description, created_by, updated_by)
SELECT 'EXRT_CASE_USERS', 'ExRT Case Users', 'Users for ExRT Request', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM groups WHERE code = 'EXRT_CASE_USERS');

INSERT INTO group_roles (group_id, role_id)
SELECT g.id, r.id FROM groups g CROSS JOIN roles r
WHERE g.code = 'EXRT_CASE_USERS' AND r.code = 'EXRT_CASE_USER'
  AND NOT EXISTS (SELECT 1 FROM group_roles gr WHERE gr.group_id = g.id AND gr.role_id = r.id);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p
WHERE r.code = 'EXRT_CASE_USER'
  AND p.code IN ('CASES_VIEW', 'CASES_CREATE', 'WF_VIEW', 'NOTIF_VIEW')
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id);

INSERT INTO role_case_types (role_id, case_type_id)
SELECT r.id, ct.id FROM roles r CROSS JOIN case_types ct
WHERE r.code = 'EXRT_CASE_USER' AND ct.code = 'EXRT_REQUEST'
  AND NOT EXISTS (SELECT 1 FROM role_case_types x WHERE x.role_id = r.id AND x.case_type_id = ct.id);

INSERT INTO role_initiating_teams (role_id, team_id)
SELECT r.id, t.id FROM roles r CROSS JOIN teams t
WHERE r.code = 'EXRT_CASE_USER' AND t.code IN ('CLIENT_SERVICES', 'LEGAL_TEAM')
  AND NOT EXISTS (SELECT 1 FROM role_initiating_teams x WHERE x.role_id = r.id AND x.team_id = t.id);

INSERT INTO role_receiving_teams (role_id, team_id)
SELECT r.id, t.id FROM roles r CROSS JOIN teams t
WHERE r.code = 'EXRT_CASE_USER' AND t.code = 'EXECUTIVE_RESPONSE_TEAM'
  AND NOT EXISTS (SELECT 1 FROM role_receiving_teams x WHERE x.role_id = r.id AND x.team_id = t.id);
