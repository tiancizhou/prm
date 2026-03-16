CREATE TABLE IF NOT EXISTS sys_company (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    name           VARCHAR(128) NOT NULL,
    short_name     VARCHAR(64),
    contact_name   VARCHAR(64),
    phone          VARCHAR(32),
    email          VARCHAR(128),
    address        VARCHAR(255),
    description    VARCHAR(500),
    status         VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    deleted        INTEGER      NOT NULL DEFAULT 0,
    created_by     BIGINT,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     BIGINT,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO sys_company (name, short_name, status, deleted, created_at, updated_at)
SELECT '默认公司', '默认公司', 'ACTIVE', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM sys_company WHERE deleted = 0
);

ALTER TABLE sys_department ADD COLUMN company_id BIGINT;

UPDATE sys_department
SET company_id = (
    SELECT id
    FROM sys_company
    WHERE deleted = 0
    ORDER BY id
    LIMIT 1
)
WHERE company_id IS NULL;

DROP INDEX IF EXISTS idx_sys_department_name_parent_deleted;

CREATE UNIQUE INDEX IF NOT EXISTS idx_sys_department_company_name_parent_deleted
    ON sys_department (company_id, name, COALESCE(parent_id, 0), deleted);

CREATE INDEX IF NOT EXISTS idx_sys_department_company_id ON sys_department (company_id);
