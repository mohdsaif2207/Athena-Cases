-- Grant Home page queue access (WF_VIEW / NOTIF_VIEW) to Charan (Billing) and Saif (DBM).
-- Cases / role / case-type scopes unchanged. Idempotent.

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
CROSS JOIN permissions p
WHERE r.code IN ('BILLING_CASE_USER', 'DBM_CASE_USER')
  AND p.code IN ('WF_VIEW', 'NOTIF_VIEW')
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = p.id
  );
