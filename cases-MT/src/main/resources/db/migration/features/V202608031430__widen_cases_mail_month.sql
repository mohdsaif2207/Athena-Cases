-- Align cases.mail_month with DBM detail / API (@Size max 32).
-- Dashboard seed used VARCHAR(7) for YYYY-MM; DBM Event lookup may return month labels.

ALTER TABLE cases
    ALTER COLUMN mail_month TYPE VARCHAR(32);
