CREATE TABLE password_recovery_codes (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    code_hash VARBINARY(64) NOT NULL,

    expires_at DATETIME(6) NOT NULL,

    failed_attempts INT NOT NULL DEFAULT 0,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at DATETIME(6) NOT NULL,

    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_password_recovery_code_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    INDEX idx_password_recovery_code_user_id (user_id),

    INDEX idx_password_recovery_code_expires_at (expires_at),

    INDEX idx_password_recovery_code_status (status)
);