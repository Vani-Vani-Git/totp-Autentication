CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    token_hash VARBINARY(64) NOT NULL,

    expires_at DATETIME(6) NOT NULL,

    failed_attempts INT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_password_reset_token_hash
        UNIQUE (token_hash),

    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_password_reset_token_user_id (user_id),

    INDEX idx_password_reset_token_expires_at (expires_at)
);