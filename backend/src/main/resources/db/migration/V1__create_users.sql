CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    totp_enabled BOOLEAN NOT NULL DEFAULT FALSE,

    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME(6) NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_users_user_id
        UNIQUE (user_id),

    CONSTRAINT uk_users_email
        UNIQUE (email),

    CONSTRAINT chk_users_status
        CHECK (status IN ('ACTIVE', 'LOCKED', 'DISABLED'))
);