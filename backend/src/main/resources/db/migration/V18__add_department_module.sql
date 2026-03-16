-- parent_id 使用 NOT NULL DEFAULT 0 表示根节点，避免 MySQL 不支持索引中使用 COALESCE() 的问题
CREATE TABLE IF NOT EXISTS sys_department (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(128) NOT NULL,
    parent_id    BIGINT       NOT NULL DEFAULT 0,
    sort_order   INT          NOT NULL DEFAULT 0,
    status       VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    deleted      INT          NOT NULL DEFAULT 0,
    created_by   BIGINT,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by   BIGINT,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE sys_user ADD COLUMN department_id BIGINT;

INSERT INTO sys_department (name, parent_id, sort_order, status, deleted, created_at, updated_at)
SELECT department_name, 0, 0, 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM (
    SELECT DISTINCT TRIM(department) AS department_name
    FROM sys_user
    WHERE department IS NOT NULL AND TRIM(department) <> '' AND deleted = 0
) t
WHERE department_name IS NOT NULL AND department_name <> '';

UPDATE sys_user
SET department_id = (
    SELECT d.id
    FROM sys_department d
    WHERE d.deleted = 0 AND d.name = TRIM(sys_user.department)
    ORDER BY d.id
    LIMIT 1
)
WHERE department IS NOT NULL AND TRIM(department) <> '';

CREATE UNIQUE INDEX idx_sys_department_name_parent_deleted
    ON sys_department (name, parent_id, deleted);

CREATE INDEX idx_sys_department_parent_id ON sys_department (parent_id);
CREATE INDEX idx_sys_user_department_id ON sys_user (department_id);
