-- Saif (DBM_CASE_USER): Cases only — revoke Home queue permissions.
-- Keeps CASES_VIEW / CASES_CREATE, DBM case type, and initiating/receiving team scopes.
-- WF_VIEW / NOTIF_VIEW can be re-granted later via Utilities (dynamic RBAC).

DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE code = 'DBM_CASE_USER')
  AND permission_id IN (
      SELECT id FROM permissions WHERE code IN ('WF_VIEW', 'NOTIF_VIEW')
  );
