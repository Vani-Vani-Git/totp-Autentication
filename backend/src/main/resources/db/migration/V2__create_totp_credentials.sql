CREATE TABLE totp_credentials (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    encrypted_secret VARBINARY(512) NOT NULL,
    encryption_iv VARBINARY(16) NOT NULL,

    issuer VARCHAR(100) NOT NULL,
    account_label VARCHAR(255) NOT NULL,

    algorithm VARCHAR(20) NOT NULL DEFAULT 'SHA1',
    digits INT NOT NULL DEFAULT 6,
    period_seconds INT NOT NULL DEFAULT 30,

    enabled BOOLEAN NOT NULL DEFAULT FALSE,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_totp_credentials_user
        UNIQUE (user_id),

    CONSTRAINT fk_totp_credentials_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_totp_algorithm
        CHECK (algorithm IN ('SHA1', 'SHA256', 'SHA512')),

    CONSTRAINT chk_totp_digits
        CHECK (digits IN (6, 8)),

    CONSTRAINT chk_totp_period
        CHECK (period_seconds > 0)
);