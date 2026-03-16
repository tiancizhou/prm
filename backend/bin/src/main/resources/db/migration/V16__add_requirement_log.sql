CREATE TABLE pm_requirement_log (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    requirement_id INTEGER NOT NULL,
    user_id        INTEGER,
    username       VARCHAR(50),
    log_type       VARCHAR(20) NOT NULL DEFAULT 'COMMENT',  -- AUTO | COMMENT
    content        VARCHAR(2000) NOT NULL,
    created_at     DATETIME NOT NULL
);

CREATE INDEX idx_req_log_req ON pm_requirement_log(requirement_id);
