-- Grant Cases Edit to Billing / DBM / ExRT receiving-team case users.
-- Idempotent. Scoped edits still enforced by case-type + receiving-team checks in services.

INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
CROSS JOIN permissions p
WHERE r.code IN ('BILLING_CASE_USER', 'DBM_CASE_USER', 'EXRT_CASE_USER')
  AND p.code = 'CASES_EDIT'
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = p.id
  );
