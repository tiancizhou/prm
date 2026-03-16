-- V15: 新增项目模块表，支持需求按模块分类
CREATE TABLE pm_module (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    project_id INTEGER NOT NULL,
    parent_id  INTEGER REFERENCES pm_module(id),
    name       VARCHAR(100) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    deleted    INTEGER NOT NULL DEFAULT 0,
    created_by INTEGER,
    created_at DATETIME,
    updated_by INTEGER,
    updated_at DATETIME
);
CREATE INDEX idx_module_project ON pm_module(project_id);

ALTER TABLE pm_requirement ADD COLUMN module_id INTEGER REFERENCES pm_module(id);
