-- Per-user UI preferences (e.g. Cases Search column layout) persisted to profile.

CREATE TABLE IF NOT EXISTS user_preferences (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    pref_key     VARCHAR(120) NOT NULL,
    pref_value   TEXT         NOT NULL,
    version      INTEGER      NOT NULL DEFAULT 1,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by   VARCHAR(64)  NOT NULL,
    updated_by   VARCHAR(64)  NOT NULL,
    CONSTRAINT uq_user_preferences_user_key UNIQUE (user_id, pref_key),
    CONSTRAINT fk_user_preferences_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_user_preferences_user_id ON user_preferences (user_id);
