CREATE TABLE authenticated_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,

    session_id CHAR(36) NOT NULL,

    user_id BIGINT NOT NULL,

    refresh_token_hash VARBINARY(64) NOT NULL,

    device_id VARCHAR(255) NULL,
    device_name VARCHAR(255) NULL,

    ip_address VARCHAR(45) NULL,

    access_token_jti CHAR(36) NOT NULL,

    expires_at DATETIME(6) NOT NULL,
    last_used_at DATETIME(6) NULL,
    revoked_at DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_authenticated_session_id
        UNIQUE (session_id),

    CONSTRAINT uk_authenticated_access_token_jti
        UNIQUE (access_token_jti),

    CONSTRAINT uk_authenticated_refresh_token
        UNIQUE (refresh_token_hash),

    CONSTRAINT fk_authenticated_session_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_authenticated_sessions_user
    ON authenticated_sessions(user_id);

CREATE INDEX idx_authenticated_sessions_expires
    ON authenticated_sessions(expires_at);

CREATE INDEX idx_authenticated_sessions_revoked
    ON authenticated_sessions(revoked_at);