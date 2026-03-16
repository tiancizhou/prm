-- V15: 新增项目模块表，支持需求按模块分类
CREATE TABLE pm_module (
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT       NOT NULL,
    parent_id  BIGINT,
    name       VARCHAR(100) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    deleted    INT          NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME,
    updated_by BIGINT,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_module_project ON pm_module(project_id);

ALTER TABLE pm_requirement ADD COLUMN module_id BIGINT;
