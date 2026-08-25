CREATE TABLE temporary_auth_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,

    session_token_hash VARBINARY(64) NOT NULL,

    user_id BIGINT NOT NULL,

    expires_at DATETIME(6) NOT NULL,

    failed_attempts INT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_temp_auth_session_token
        UNIQUE (session_token_hash),

    CONSTRAINT fk_temp_auth_session_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_temp_auth_session_status
        CHECK (status IN ('PENDING', 'VERIFIED', 'EXPIRED', 'LOCKED'))
);

CREATE INDEX idx_temp_auth_sessions_user
    ON temporary_auth_sessions(user_id);

CREATE INDEX idx_temp_auth_sessions_expires
    ON temporary_auth_sessions(expires_at);