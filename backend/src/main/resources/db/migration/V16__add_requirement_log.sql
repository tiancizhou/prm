CREATE TABLE pm_requirement_log (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    requirement_id BIGINT       NOT NULL,
    user_id        BIGINT,
    username       VARCHAR(50),
    log_type       VARCHAR(20)  NOT NULL DEFAULT 'COMMENT',
    content        VARCHAR(2000) NOT NULL,
    created_at     DATETIME     NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_req_log_req ON pm_requirement_log(requirement_id);
