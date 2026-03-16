-- ==========================================
-- V2: 项目管理核心业务表
-- ==========================================

-- 项目
CREATE TABLE IF NOT EXISTS pm_project (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         VARCHAR(128) NOT NULL,
    code         VARCHAR(64)  NOT NULL UNIQUE,
    description  TEXT,
    status       VARCHAR(32)  NOT NULL DEFAULT 'ACTIVE',
    visibility   VARCHAR(16)  NOT NULL DEFAULT 'PRIVATE',
    owner_id     BIGINT       NOT NULL,
    start_date   DATE,
    end_date     DATE,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    created_by   BIGINT       NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_by   BIGINT       NOT NULL DEFAULT 0,
    updated_at   DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 项目成员
CREATE TABLE IF NOT EXISTS pm_project_member (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    role       VARCHAR(32) NOT NULL DEFAULT 'DEV',
    created_at DATETIME    NOT NULL DEFAULT (datetime('now')),
    UNIQUE(project_id, user_id)
);

-- 需求
CREATE TABLE IF NOT EXISTS pm_requirement (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id       BIGINT       NOT NULL,
    sprint_id        BIGINT,
    title            VARCHAR(256) NOT NULL,
    description      TEXT,
    source           VARCHAR(64),
    priority         VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    status           VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',
    assignee_id      BIGINT,
    estimated_hours  DECIMAL(8,1) NOT NULL DEFAULT 0,
    acceptance_criteria TEXT,
    deleted          TINYINT      NOT NULL DEFAULT 0,
    created_by       BIGINT       NOT NULL DEFAULT 0,
    created_at       DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_by       BIGINT       NOT NULL DEFAULT 0,
    updated_at       DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 需求评审记录
CREATE TABLE IF NOT EXISTS pm_requirement_review (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    requirement_id   BIGINT       NOT NULL,
    reviewer_id      BIGINT       NOT NULL,
    conclusion       VARCHAR(32)  NOT NULL,
    remark           TEXT,
    reviewed_at      DATETIME     NOT NULL DEFAULT (datetime('now')),
    created_by       BIGINT       NOT NULL DEFAULT 0,
    created_at       DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 需求关联（需求-需求、需求-Bug 多对多）
CREATE TABLE IF NOT EXISTS pm_requirement_link (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    source_type  VARCHAR(32) NOT NULL,
    source_id    BIGINT      NOT NULL,
    target_type  VARCHAR(32) NOT NULL,
    target_id    BIGINT      NOT NULL,
    link_type    VARCHAR(32) NOT NULL DEFAULT 'RELATE',
    created_by   BIGINT      NOT NULL DEFAULT 0,
    created_at   DATETIME    NOT NULL DEFAULT (datetime('now')),
    UNIQUE(source_type, source_id, target_type, target_id)
);

-- 任务
CREATE TABLE IF NOT EXISTS pm_task (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id       BIGINT       NOT NULL,
    requirement_id   BIGINT,
    sprint_id        BIGINT,
    parent_task_id   BIGINT,
    title            VARCHAR(256) NOT NULL,
    description      TEXT,
    type             VARCHAR(32)  NOT NULL DEFAULT 'TASK',
    priority         VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    status           VARCHAR(32)  NOT NULL DEFAULT 'TODO',
    assignee_id      BIGINT,
    estimated_hours  DECIMAL(8,1) NOT NULL DEFAULT 0,
    spent_hours      DECIMAL(8,1) NOT NULL DEFAULT 0,
    remaining_hours  DECIMAL(8,1) NOT NULL DEFAULT 0,
    due_date         DATE,
    is_blocked       TINYINT      NOT NULL DEFAULT 0,
    deleted          TINYINT      NOT NULL DEFAULT 0,
    created_by       BIGINT       NOT NULL DEFAULT 0,
    created_at       DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_by       BIGINT       NOT NULL DEFAULT 0,
    updated_at       DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 任务依赖关系
CREATE TABLE IF NOT EXISTS pm_task_dependency (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id     BIGINT      NOT NULL,
    depends_on  BIGINT      NOT NULL,
    dep_type    VARCHAR(32) NOT NULL DEFAULT 'BLOCKS',
    created_at  DATETIME    NOT NULL DEFAULT (datetime('now')),
    UNIQUE(task_id, depends_on)
);

-- 工时日志
CREATE TABLE IF NOT EXISTS pm_task_worklog (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id     BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    spent_hours DECIMAL(8,1) NOT NULL,
    log_date    DATE         NOT NULL,
    remark      TEXT,
    created_at  DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 缺陷
CREATE TABLE IF NOT EXISTS pm_bug (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id      BIGINT       NOT NULL,
    sprint_id       BIGINT,
    title           VARCHAR(256) NOT NULL,
    description     TEXT,
    module          VARCHAR(128),
    severity        VARCHAR(16)  NOT NULL DEFAULT 'NORMAL',
    priority        VARCHAR(16)  NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(32)  NOT NULL DEFAULT 'NEW',
    assignee_id     BIGINT,
    reporter_id     BIGINT       NOT NULL,
    steps           TEXT,
    expected_result TEXT,
    actual_result   TEXT,
    environment     VARCHAR(256),
    resolve_type    VARCHAR(32),
    resolved_at     DATETIME,
    verified_at     DATETIME,
    deleted         TINYINT      NOT NULL DEFAULT 0,
    created_by      BIGINT       NOT NULL DEFAULT 0,
    created_at      DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_by      BIGINT       NOT NULL DEFAULT 0,
    updated_at      DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 缺陷评论
CREATE TABLE IF NOT EXISTS pm_bug_comment (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    bug_id     BIGINT   NOT NULL,
    user_id    BIGINT   NOT NULL,
    content    TEXT     NOT NULL,
    parent_id  BIGINT,
    deleted    TINYINT  NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT (datetime('now')),
    updated_at DATETIME NOT NULL DEFAULT (datetime('now'))
);

-- 迭代/Sprint
CREATE TABLE IF NOT EXISTS pm_sprint (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id       BIGINT       NOT NULL,
    name             VARCHAR(128) NOT NULL,
    goal             TEXT,
    status           VARCHAR(32)  NOT NULL DEFAULT 'PLANNING',
    capacity_hours   DECIMAL(8,1) NOT NULL DEFAULT 0,
    start_date       DATE,
    end_date         DATE,
    closed_at        DATETIME,
    deleted          TINYINT      NOT NULL DEFAULT 0,
    created_by       BIGINT       NOT NULL DEFAULT 0,
    created_at       DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_by       BIGINT       NOT NULL DEFAULT 0,
    updated_at       DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 迭代条目（Sprint 关联的需求/任务）
CREATE TABLE IF NOT EXISTS pm_sprint_item (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    sprint_id  BIGINT      NOT NULL,
    item_type  VARCHAR(32) NOT NULL,
    item_id    BIGINT      NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT (datetime('now')),
    UNIQUE(sprint_id, item_type, item_id)
);

-- 版本发布
CREATE TABLE IF NOT EXISTS pm_release (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id   BIGINT       NOT NULL,
    sprint_id    BIGINT,
    version      VARCHAR(64)  NOT NULL,
    description  TEXT,
    status       VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',
    release_date DATE,
    released_by  BIGINT,
    released_at  DATETIME,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    created_by   BIGINT       NOT NULL DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_by   BIGINT       NOT NULL DEFAULT 0,
    updated_at   DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 发布条目
CREATE TABLE IF NOT EXISTS pm_release_item (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    release_id  BIGINT      NOT NULL,
    item_type   VARCHAR(32) NOT NULL,
    item_id     BIGINT      NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT (datetime('now')),
    UNIQUE(release_id, item_type, item_id)
);

-- 附件
CREATE TABLE IF NOT EXISTS pm_attachment (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    biz_type    VARCHAR(32)  NOT NULL,
    biz_id      BIGINT       NOT NULL,
    filename    VARCHAR(256) NOT NULL,
    filepath    VARCHAR(512) NOT NULL,
    file_size   BIGINT       NOT NULL DEFAULT 0,
    mime_type   VARCHAR(128),
    uploader_id BIGINT       NOT NULL,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT (datetime('now'))
);

-- 通用评论
CREATE TABLE IF NOT EXISTS pm_comment (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    biz_type   VARCHAR(32) NOT NULL,
    biz_id     BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    content    TEXT        NOT NULL,
    parent_id  BIGINT,
    deleted    TINYINT     NOT NULL DEFAULT 0,
    created_at DATETIME    NOT NULL DEFAULT (datetime('now')),
    updated_at DATETIME    NOT NULL DEFAULT (datetime('now'))
);

-- 操作日志
CREATE TABLE IF NOT EXISTS pm_operation_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     BIGINT      NOT NULL,
    username    VARCHAR(64),
    module      VARCHAR(64) NOT NULL,
    action      VARCHAR(64) NOT NULL,
    biz_type    VARCHAR(32),
    biz_id      BIGINT,
    before_data TEXT,
    after_data  TEXT,
    ip          VARCHAR(64),
    user_agent  VARCHAR(512),
    duration_ms BIGINT,
    created_at  DATETIME    NOT NULL DEFAULT (datetime('now'))
);

-- 看板快照（每日聚合）
CREATE TABLE IF NOT EXISTS pm_dashboard_snapshot (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id   BIGINT   NOT NULL,
    snap_date    DATE     NOT NULL,
    metrics_json TEXT     NOT NULL,
    created_at   DATETIME NOT NULL DEFAULT (datetime('now')),
    UNIQUE(project_id, snap_date)
);

-- 创建常用索引
CREATE INDEX IF NOT EXISTS idx_requirement_project  ON pm_requirement(project_id);
CREATE INDEX IF NOT EXISTS idx_requirement_sprint   ON pm_requirement(sprint_id);
CREATE INDEX IF NOT EXISTS idx_task_project         ON pm_task(project_id);
CREATE INDEX IF NOT EXISTS idx_task_sprint          ON pm_task(sprint_id);
CREATE INDEX IF NOT EXISTS idx_task_assignee        ON pm_task(assignee_id);
CREATE INDEX IF NOT EXISTS idx_bug_project          ON pm_bug(project_id);
CREATE INDEX IF NOT EXISTS idx_bug_sprint           ON pm_bug(sprint_id);
CREATE INDEX IF NOT EXISTS idx_bug_assignee         ON pm_bug(assignee_id);
CREATE INDEX IF NOT EXISTS idx_operation_log_biz    ON pm_operation_log(biz_type, biz_id);
CREATE INDEX IF NOT EXISTS idx_dashboard_project    ON pm_dashboard_snapshot(project_id, snap_date);
CREATE INDEX IF NOT EXISTS idx_attachment_biz       ON pm_attachment(biz_type, biz_id);
