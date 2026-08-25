CREATE TABLE totp_consumed_steps (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NOT NULL,

    time_step BIGINT NOT NULL,

    used_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_totp_consumed_user_step
        UNIQUE (user_id, time_step),

    CONSTRAINT fk_totp_consumed_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_totp_consumed_steps_user
    ON totp_consumed_steps(user_id);

CREATE INDEX idx_totp_consumed_steps_used_at
    ON totp_consumed_steps(used_at);