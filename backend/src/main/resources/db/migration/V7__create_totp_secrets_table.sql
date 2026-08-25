CREATE TABLE totp_secrets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    encrypted_secret TEXT NOT NULL,
    algorithm VARCHAR(20) NOT NULL,
    digits INT NOT NULL,
    period_seconds INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_totp_secrets_user
        UNIQUE (user_id),

    CONSTRAINT fk_totp_secrets_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
);