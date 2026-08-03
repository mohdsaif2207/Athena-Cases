-- Align ExRT case type display name with Cases Dashboard New Case popup.
UPDATE case_types
SET name = 'Executive Response Team (ExRT) Request',
    updated_at = NOW(),
    updated_by = 'SYSTEM'
WHERE code = 'EXRT_REQUEST'
  AND name <> 'Executive Response Team (ExRT) Request';
