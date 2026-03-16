-- ==========================================
-- V1: 系统基础表（用户、角色、权限、团队）
-- ==========================================

CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(64),
    email       VARCHAR(128),
    phone       VARCHAR(20),
    avatar      VARCHAR(512),
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_by  BIGINT       NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT       NOT NULL DEFAULT 0,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    description VARCHAR(256),
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_by  BIGINT       NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT       NOT NULL DEFAULT 0,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_permission (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    code        VARCHAR(128) NOT NULL UNIQUE,
    type        VARCHAR(16)  NOT NULL DEFAULT 'API',
    parent_id   BIGINT       NOT NULL DEFAULT 0,
    sort        INT          NOT NULL DEFAULT 0,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE(role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE(user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_team (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64)  NOT NULL,
    description VARCHAR(256),
    leader_id   BIGINT,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    created_by  BIGINT       NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  BIGINT       NOT NULL DEFAULT 0,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS sys_team_member (
    id      BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT      NOT NULL,
    user_id BIGINT      NOT NULL,
    role    VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
    UNIQUE(team_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化超级管理员账号（密码: Admin@123，BCrypt哈希）
-- 默认密码: Admin@123
INSERT IGNORE INTO sys_user (id, username, password, nickname, status, created_by, updated_by)
VALUES (1, 'admin', '$2a$10$d2GYzgRgLrVidaEj0xwVUuCSFHYsUhYqh4mlEKOL4wo0PbFD/qr1y', '超级管理员', 'ACTIVE', 0, 0);

-- 初始化系统角色
INSERT IGNORE INTO sys_role (id, name, code, description, created_by, updated_by)
VALUES
    (1, '超级管理员', 'SUPER_ADMIN',    '系统最高权限', 0, 0),
    (2, '项目经理', 'PROJECT_ADMIN',  '管理所有项目', 0, 0),
    (3, '产品经理',   'PM',             '需求管理权限', 0, 0),
    (4, '开发',       'DEV',            '任务开发权限', 0, 0),
    (5, '测试',       'QA',             'Bug管理权限',  0, 0),
    (6, '访客',       'GUEST',          '只读权限',     0, 0);

-- 为 admin 分配超级管理员角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);
