CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,

    user_id BIGINT NULL,

    event_type VARCHAR(50) NOT NULL,

    outcome VARCHAR(20) NOT NULL,

    ip_address VARCHAR(45) NULL,

    device_id VARCHAR(255) NULL,

    correlation_id VARCHAR(64) NOT NULL,

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT fk_audit_log_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CONSTRAINT chk_audit_log_outcome
        CHECK (outcome IN ('SUCCESS', 'FAILURE', 'REJECTED'))
);

CREATE INDEX idx_audit_logs_user
    ON audit_logs(user_id);

CREATE INDEX idx_audit_logs_event_type
    ON audit_logs(event_type);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs(created_at);

CREATE INDEX idx_audit_logs_correlation_id
    ON audit_logs(correlation_id);