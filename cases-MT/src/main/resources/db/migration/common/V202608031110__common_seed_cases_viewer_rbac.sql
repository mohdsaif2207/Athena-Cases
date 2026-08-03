-- Cases Viewer RBAC: view Cases screen only (no admin / create / edit / workflow / notification).

INSERT INTO roles (code, name, description, created_by, updated_by)
SELECT 'CASES_VIEWER', 'Cases Viewer', 'View-only access to Cases screen', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE code = 'CASES_VIEWER');

INSERT INTO groups (code, name, description, created_by, updated_by)
SELECT 'CASES_VIEWERS', 'Cases Viewers', 'Users who may view Cases only', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM groups WHERE code = 'CASES_VIEWERS');

INSERT INTO group_roles (group_id, role_id)
SELECT g.id, r.id
FROM groups g
CROSS JOIN roles r
WHERE g.code = 'CASES_VIEWERS'
  AND r.code = 'CASES_VIEWER'
  AND NOT EXISTS (
      SELECT 1 FROM group_roles gr
      WHERE gr.group_id = g.id AND gr.role_id = r.id
  );

-- View-only: CASES_VIEW only (not CASES_ACCESS — that also unlocks create/edit/export in UI)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'CASES_VIEWER'
  AND p.code = 'CASES_VIEW'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- Allow viewing cases across active case types (scoped via role_case_types)
INSERT INTO role_case_types (role_id, case_type_id)
SELECT r.id, ct.id
FROM roles r
CROSS JOIN case_types ct
WHERE r.code = 'CASES_VIEWER'
  AND ct.active = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM role_case_types rct
      WHERE rct.role_id = r.id AND rct.case_type_id = ct.id
  );
