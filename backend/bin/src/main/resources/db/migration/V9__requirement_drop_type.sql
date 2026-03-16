-- V9: 需求取消类型区分（小团队/轻流程，不需要 Epic/Story 区分）
-- SQLite 不支持 DROP COLUMN 直接语法，通过重建表来删除 type 列

CREATE TABLE pm_requirement_new (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id       INTEGER NOT NULL,
    sprint_id        INTEGER,
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    source           VARCHAR(255),
    priority         VARCHAR(32)  NOT NULL DEFAULT 'MEDIUM',
    status           VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',
    assignee_id      INTEGER,
    estimated_hours  DECIMAL(8,2) NOT NULL DEFAULT 0,
    acceptance_criteria TEXT,
    start_date       DATE,
    due_date         DATE,
    deleted          INTEGER      NOT NULL DEFAULT 0,
    created_by       INTEGER,
    created_at       DATETIME,
    updated_by       INTEGER,
    updated_at       DATETIME
);

INSERT INTO pm_requirement_new
    (id, project_id, sprint_id, title, description, source,
     priority, status, assignee_id, estimated_hours, acceptance_criteria,
     start_date, due_date, deleted, created_by, created_at, updated_by, updated_at)
SELECT id, project_id, sprint_id, title, description, source,
       priority, status, assignee_id, estimated_hours, acceptance_criteria,
       start_date, due_date, deleted, created_by, created_at, updated_by, updated_at
FROM pm_requirement;

DROP TABLE pm_requirement;
ALTER TABLE pm_requirement_new RENAME TO pm_requirement;

CREATE INDEX IF NOT EXISTS idx_requirement_due_date ON pm_requirement(due_date);
